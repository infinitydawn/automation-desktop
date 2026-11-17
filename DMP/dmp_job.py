"""Read, edit and build DMP Remote Link job exports ("DMP Panel Information (*.xml)").

File format (Remote Link 2.52):
  - The .xml file is uppercase hex of AES-128-ECB ciphertext. The AES key is MD5 of the
    encryption key typed in the export dialog.
  - The plaintext is <Panels><Panel>...</Panel></Panels>, one child per Remote Link screen
    (Account, CommPathList, ZoneInfoList, OutNamesList, SysOpts, ...). Bytes after </Panels>
    are just padding up to the 16-byte block size.
  - Every value has a DataType attribute: 1 = text, stored as base64 of the text padded with
    NUL bytes to a multiple of 3; 3 = integer; 5 = True/False; 11 = date/time; 14 = record ID.

Commands:
  python dmp_job.py build job.ini            make a new job file from a template + job.ini
  python dmp_job.py dump  job.xml            print panel, comm path, outputs and zones
  python dmp_job.py decrypt job.xml -o x.xml write the readable XML (for looking around)
  python dmp_job.py encrypt x.xml -o job.xml encrypt a readable XML back into a job file
Encryption key: --key NNNN, else the DMP_EXPORT_KEY environment variable, else
`key = NNNN` under [dmp] in settings.ini next to this script (settings.ini is gitignored,
so the key never goes into the repo).
"""
import argparse
import base64
import configparser
import copy
import csv
import datetime
import hashlib
import os
import re
import sys
import xml.etree.ElementTree as ET

SETTINGS_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "settings.ini")


def default_key():
    """The team's export key, kept out of the repo (env var or gitignored settings.ini)."""
    key = os.environ.get("DMP_EXPORT_KEY", "").strip()
    if not key and os.path.isfile(SETTINGS_FILE):
        cp = configparser.ConfigParser(interpolation=None)
        cp.read(SETTINGS_FILE, encoding="utf-8-sig")
        key = cp.get("dmp", "key", fallback="").strip()
    if not key:
        raise SystemExit(f"No export encryption key. Create {SETTINGS_FILE} containing:\n"
                         f"  [dmp]\n  key = <the key you type when exporting in Remote Link>\n"
                         f"or set DMP_EXPORT_KEY, or pass --key.")
    return key

# ---------------------------------------------------------------- AES-128 (ECB only)
# Plain-Python AES so the tool needs nothing beyond a standard Python install.

def _xtime(a):
    a <<= 1
    return (a ^ 0x11B) & 0xFF if a & 0x100 else a


def _mul(a, b):
    r = 0
    while b:
        if b & 1:
            r ^= a
        a = _xtime(a)
        b >>= 1
    return r


def _build_sbox():
    sbox = [0] * 256
    p = q = 1
    while True:
        p = p ^ _xtime(p)                      # multiply p by 3
        q ^= q << 1                            # divide q by 3
        q ^= q << 2
        q ^= q << 4
        q &= 0xFF
        if q & 0x80:
            q ^= 0x09
        x = q ^ (q << 1 | q >> 7) ^ (q << 2 | q >> 6) ^ (q << 3 | q >> 5) ^ (q << 4 | q >> 4)
        sbox[p] = (x ^ 0x63) & 0xFF
        if p == 1:
            break
    sbox[0] = 0x63
    return sbox


_SBOX = _build_sbox()
_INV_SBOX = [0] * 256
for _i, _v in enumerate(_SBOX):
    _INV_SBOX[_v] = _i


def _expand_key(key):
    words = [list(key[i:i + 4]) for i in range(0, 16, 4)]
    rcon = 1
    for i in range(4, 44):
        t = list(words[i - 1])
        if i % 4 == 0:
            t = t[1:] + t[:1]
            t = [_SBOX[b] for b in t]
            t[0] ^= rcon
            rcon = _xtime(rcon)
        words.append([a ^ b for a, b in zip(words[i - 4], t)])
    return [sum(words[r * 4:r * 4 + 4], []) for r in range(11)]


_SHIFT = [0, 5, 10, 15, 4, 9, 14, 3, 8, 13, 2, 7, 12, 1, 6, 11]
_INV_SHIFT = [_SHIFT.index(i) for i in range(16)]


def _encrypt_block(s, rk):
    s = [a ^ b for a, b in zip(s, rk[0])]
    for r in range(1, 11):
        s = [_SBOX[s[i]] for i in _SHIFT]
        if r != 10:
            m = []
            for c in range(4):
                a = s[c * 4:c * 4 + 4]
                m += [_mul(a[0], 2) ^ _mul(a[1], 3) ^ a[2] ^ a[3],
                      a[0] ^ _mul(a[1], 2) ^ _mul(a[2], 3) ^ a[3],
                      a[0] ^ a[1] ^ _mul(a[2], 2) ^ _mul(a[3], 3),
                      _mul(a[0], 3) ^ a[1] ^ a[2] ^ _mul(a[3], 2)]
            s = m
        s = [a ^ b for a, b in zip(s, rk[r])]
    return s


def _decrypt_block(s, rk):
    s = [a ^ b for a, b in zip(s, rk[10])]
    for r in range(9, -1, -1):
        s = [_INV_SBOX[s[i]] for i in _INV_SHIFT]
        s = [a ^ b for a, b in zip(s, rk[r])]
        if r != 0:
            m = []
            for c in range(4):
                a = s[c * 4:c * 4 + 4]
                m += [_mul(a[0], 14) ^ _mul(a[1], 11) ^ _mul(a[2], 13) ^ _mul(a[3], 9),
                      _mul(a[0], 9) ^ _mul(a[1], 14) ^ _mul(a[2], 11) ^ _mul(a[3], 13),
                      _mul(a[0], 13) ^ _mul(a[1], 9) ^ _mul(a[2], 14) ^ _mul(a[3], 11),
                      _mul(a[0], 11) ^ _mul(a[1], 13) ^ _mul(a[2], 9) ^ _mul(a[3], 14)]
            s = m
    return s


def _aes_ecb(data, key, decrypt):
    rk = _expand_key(key)
    fn = _decrypt_block if decrypt else _encrypt_block
    out = bytearray()
    for i in range(0, len(data), 16):
        out += bytes(fn(list(data[i:i + 16]), rk))
    return bytes(out)


# ---------------------------------------------------------------- file level

def _aes_key(key):
    return hashlib.md5(str(key).encode("ascii")).digest()


def decrypt_file(path, key):
    """Return the readable XML text of a Remote Link job export."""
    if not os.path.isfile(path):
        raise SystemExit(f"job file not found: {path}")
    text = open(path, encoding="latin1").read().strip()
    try:
        data = bytes.fromhex(text)
    except ValueError:
        raise SystemExit(f"{path}: not a Remote Link job export (expected hex text)")
    plain = _aes_ecb(data, _aes_key(key), decrypt=True).decode("latin1")
    end = plain.rfind("</Panels>")
    if not plain.startswith("<Panels>") or end < 0:
        raise SystemExit(f"{path}: could not decrypt - wrong encryption key?")
    return plain[:end + len("</Panels>")]


def encrypt_text(xml_text, key):
    data = xml_text.encode("latin1")
    data += b"\0" * (-len(data) % 16)
    return _aes_ecb(data, _aes_key(key), decrypt=False).hex().upper()


def write_job(path, xml_text, key):
    with open(path, "w", encoding="ascii", newline="") as f:
        f.write(encrypt_text(xml_text, key))


# ---------------------------------------------------------------- field helpers

def get(el, tag):
    """Value of a child field as Python text (DataType 1 decoded)."""
    e = el.find(tag)
    if e is None:
        return None
    if e.get("DataType") == "1":
        return base64.b64decode(e.text or "").split(b"\0")[0].decode("latin1")
    return e.text


def put(el, tag, value):
    e = el.find(tag)
    if e is None:
        raise SystemExit(f"field {tag} missing from <{el.tag}> in the template")
    if e.get("DataType") == "1":
        raw = str(value).encode("latin1")
        raw += b"\0" * ((-len(raw) % 3) if raw else 3)
        e.text = base64.b64encode(raw).decode("ascii")
    elif e.get("DataType") == "5":
        e.text = "True" if value in (True, "True", "true", "yes", "1") else "False"
    else:
        e.text = str(value)


def now_stamp():
    return datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.000")


# ---------------------------------------------------------------- zone rules
# From the Device Cheat Sheet (Fire). First match wins, so specific names come first.
# (pattern, TYPE, AO message, AS message, note)
#   TYPE: FI = Fire, SV = Supervisory, A1 = Auxiliary 1    messages: A = Alarm, T = Trouble
ZONE_RULES = [
    (r"RELAY.*BELL|WATER ?FLOW BELL|\bWF BELL", "SV", "T", "A", None),
    (r"\bACME\b.*\b(TRBL|TROUBLE)\b", "SV", "T", "T", None),
    (r"\bACME\b", "FI", "T", "A", None),
    (r"\b(TRBL|TROUBLE)\b", "SV", "T", "T", None),
    (r"\bCO\b|CARBON", "SV", "T", "A", "co"),
    (r"REPEATER", "A1", "T", "T", "repeater"),
    (r"VALVE|TAMPER|\bVT\b|\bPIV\b|OS&Y|B4 ?& ?AFTER|BEFORE ?& ?AFTER|BACKFLOW|PUMP|JOCKEY|PS40|"
     r"PRESSURE|LOW AIR|HIGH AIR|DRY SYS",
     "SV", "T", "T", None),
    (r"WATER ?FLOW|\bWF\b|PS10|FLOW SW", "FI", "T", "A", None),
    (r"SMOKE|HEAT|PULL|\bP/?S\b|DUCT|RECALL|\bELEV|BEAM|FLAME", "FI", "T", "A", None),
]
TYPE_CODES_SEEN = {"FI", "SV"}          # codes confirmed in a real export
TYPE_NAMES = {"FI": "Fire", "SV": "Supervisory", "A1": "Auxiliary 1"}
MSG_NAMES = {"A": "Alarm", "T": "Trouble"}
ZONE_NAME_MAX = 32


def classify(name):
    for pattern, ztype, ao, as_, note in ZONE_RULES:
        if re.search(pattern, name):
            return ztype, ao, as_, note
    return None


# ---------------------------------------------------------------- job.ini parsing

def read_ini(path):
    cp = configparser.ConfigParser(interpolation=None, delimiters=("=",),
                                   comment_prefixes=("#", ";"), inline_comment_prefixes=None)
    cp.optionxform = str
    with open(path, encoding="utf-8-sig") as f:
        cp.read_file(f)
    return cp


def parse_zone_line(number, rest, where):
    """'PULL STATION BY FACP | out=1 | type=SV as=T' -> dict."""
    parts = [p.strip() for p in rest.split("|")]
    z = {"number": number, "name": parts[0].upper(), "where": where}
    for extra in parts[1:]:
        for item in extra.split():
            if "=" not in item:
                raise SystemExit(f"{where}: can't read '{item}' (use out=N, type=XX, ao=X, as=X)")
            k, v = item.split("=", 1)
            k = k.lower()
            if k not in ("out", "type", "ao", "as"):
                raise SystemExit(f"{where}: unknown option '{k}' (use out, type, ao, as)")
            z[k] = v.upper()
    return z


def read_zone_file(path):
    """Zone list file. .csv: number,name[,out] (header row optional).
    Anything else: one zone per line, '<number> <name>', e.g. '5 WATERFLOW 1FL SPRKLR RM'."""
    zones = []
    with open(path, encoding="utf-8-sig", newline="") as f:
        if path.lower().endswith(".csv"):
            rows = list(csv.reader(f))
        else:
            rows = []
            for ln in f:
                m = re.match(r"\s*(\d+)[\s,.:)\-]+(.*)", ln)
                rows.append([m.group(1), m.group(2)] if m else ln.split(None, 1))
    for i, row in enumerate(rows, 1):
        row = [c.strip() for c in row]
        if not row or not row[0]:
            continue
        if not row[0].isdigit():
            if i == 1:
                continue                     # header
            raise SystemExit(f"{path} line {i}: zone number '{row[0]}' is not a number")
        rest = row[1] if len(row) > 1 else ""
        if len(row) > 2 and row[2]:
            rest += f" | out={row[2]}"
        zones.append(parse_zone_line(int(row[0]), rest, f"{path} line {i}"))
    return zones


# ---------------------------------------------------------------- build

class Report:
    def __init__(self):
        self.changes, self.warnings = [], []

    def change(self, msg):
        self.changes.append(msg)

    def warn(self, msg):
        self.warnings.append(msg)


def _parse_time(text):
    for fmt in ("%I:%M %p", "%I:%M%p", "%H:%M"):
        try:
            t = datetime.datetime.strptime(text.strip().upper(), fmt)
            return f"1899-12-30 {t:%H:%M}:00.000"
        except ValueError:
            pass
    raise SystemExit(f"[comm] test_time '{text}' - use e.g. 10:00 AM")


def apply_panel(panel, sec, rep):
    acct = panel.find("Account")
    simple = {"name": "NAME", "customer": "CUSTOMER", "region": "REGION", "address": "ADDRESS",
              "city": "CITY", "state": "STATE", "zip": "ZIP", "account": "ACCOUNT_NUM",
              "receiver": "RECVR_NUM", "ip": "PANEL_IP", "port": "PANEL_IP_PRT",
              "phone": "PANEL_PHONE"}
    for key in sec:
        if key not in simple and key != "keep_serial":
            raise SystemExit(f"[panel] unknown setting '{key}' (allowed: {', '.join(simple)}, keep_serial)")
    for key, tag in simple.items():
        if key in sec and sec[key] != "":
            old = get(acct, tag)
            put(acct, tag, sec[key])
            if old != sec[key]:
                rep.change(f"Panel {key}: {old} -> {sec[key]}")
    if "account" in sec:
        for area in panel.findall("AreaInfoList/AreaInfo"):
            put(area, "ACCT_NUM", sec["account"])
    if sec.get("keep_serial", "no").lower() not in ("yes", "true", "1"):
        for tag in ("SERIAL_NUMBER", "MAC_ADDRESS"):
            if get(acct, tag):
                rep.change(f"Panel {tag} cleared (was {get(acct, tag)})")
                put(acct, tag, "")
    put(acct, "LAST_CHANGE", now_stamp())


def apply_comm(panel, sec, panel_sec, rep):
    paths = panel.findall("CommPathList/CommPath")
    if not paths:
        raise SystemExit("template has no communication path to edit")
    cp = paths[0]
    allowed = {"account": "ACCT_NUM", "first_phone": "FIRST_PHON", "second_phone": "SEC_PHONE",
               "transmit_delay": "TRN_DELAY", "test_time": "TEST_TIME"}
    for key in sec:
        if key not in allowed:
            raise SystemExit(f"[comm] unknown setting '{key}' (allowed: {', '.join(allowed)})")
    values = dict(sec)
    if "account" not in values and "account" in panel_sec:
        values["account"] = panel_sec["account"]
    for key, tag in allowed.items():
        if values.get(key, "") == "":
            continue
        v = _parse_time(values[key]) if key == "test_time" else values[key].strip()
        if key.endswith("phone") and not re.fullmatch(r"[0-9A-Z]*", v):
            raise SystemExit(f"[comm] {key} '{v}' - digits only, no dashes or spaces")
        old = get(cp, tag)
        put(cp, tag, v)
        if old != v:
            rep.change(f"Comm path 1 {key}: {old} -> {v}")
    put(cp, "LAST_CHANGE", now_stamp())
    if len(paths) > 1:
        rep.warn(f"template has {len(paths)} comm paths; only path 1 was edited")


def apply_outputs(panel, sec, rep):
    lst = panel.find("OutNamesList")
    existing = lst.findall("OutNames")
    if not existing:
        raise SystemExit("[outputs] given but the template has no outputs to copy from - "
                         "use a template job that has at least one output")
    base = existing[0]
    for e in existing:
        lst.remove(e)
    for key, name in sorted(sec.items(), key=lambda kv: int(kv[0])):
        if not key.isdigit():
            raise SystemExit(f"[outputs] '{key}' is not an output number")
        o = copy.deepcopy(base)
        put(o, "NUMBER", int(key))
        put(o, "NAME", name.strip().upper())
        put(o, "LAST_CHANGE", now_stamp())
        lst.append(o)
        rep.change(f"Output {key}: {name.strip().upper()}")


def apply_zones(panel, zones, rep):
    lst = panel.find("ZoneInfoList")
    old = lst.findall("ZoneInfo")
    if not old:
        raise SystemExit("template has no zones to copy settings from")
    by_type = {}
    for z in old:
        by_type.setdefault(get(z, "TYPE"), z)
    seen, errors, has_co = set(), [], False
    built = []
    for z in zones:
        n, name = z["number"], z["name"]
        if n in seen:
            errors.append(f"zone {n} is listed twice ({z['where']})")
            continue
        seen.add(n)
        if not name:
            errors.append(f"zone {n} has no name ({z['where']})")
            continue
        if len(name) > ZONE_NAME_MAX:
            errors.append(f"zone {n} name is {len(name)} characters, max {ZONE_NAME_MAX}: {name}")
            continue
        rule = classify(name)
        if rule is None and "type" not in z:
            errors.append(f"zone {n} '{name}': can't tell the device type from the name - "
                          f"add '| type=FI' (Fire) or '| type=SV' (Supervisory)")
            continue
        ztype, ao, as_, note = rule or (z["type"], "T", "A" if z["type"] == "FI" else "T", None)
        ztype, ao, as_ = z.get("type", ztype), z.get("ao", ao), z.get("as", as_)
        if note == "co":
            has_co = True
        if ztype not in TYPE_CODES_SEEN:
            rep.warn(f"zone {n} '{name}': type code {ztype} hasn't been seen in a real export yet - "
                     f"check it in Remote Link after import")
        base = by_type.get(ztype, old[0])
        e = copy.deepcopy(base)
        put(e, "NUMBER", n)
        put(e, "NAME", name)
        put(e, "TYPE", ztype)
        put(e, "AO_ACT_MSG", ao)
        put(e, "AS_ACT_MSG", as_)
        out = z.get("out")
        put(e, "AS_OUT_NUM", int(out) if out else 0)
        put(e, "AS_OUT_ACT", "S" if out else "-")
        put(e, "LAST_CHANGE", now_stamp())
        built.append((n, e))
        label = f"{TYPE_NAMES.get(ztype, ztype)}, AO {MSG_NAMES.get(ao, ao)}, AS {MSG_NAMES.get(as_, as_)}"
        rep.change(f"Zone {n:>3} {name:<32} {label}" + (f", AS output {out} Steady" if out else ""))
    if errors:
        raise SystemExit("Zone list problems:\n  " + "\n  ".join(errors))
    for e in old:
        lst.remove(e)
    for _, e in sorted(built, key=lambda t: t[0]):
        lst.append(e)
    # Latched supervisory: ON only when there is a CO detector (System Options notes).
    so = panel.find("SysOpts")
    want = "True" if has_co else "False"
    if get(so, "LATCH_SV") != want:
        put(so, "LATCH_SV", want)
        rep.change(f"System Options latched supervisory -> {'ON (CO detector present)' if has_co else 'OFF'}")
    outs = {get(o, "NUMBER") for o in panel.findall("OutNamesList/OutNames")}
    for n, e in built:
        o = get(e, "AS_OUT_NUM")
        if o != "0" and o not in outs:
            rep.warn(f"zone {n} uses AS output {o}, but there is no output {o} in Output Information")


def build(ini_path, key_override=None):
    ini_dir = os.path.dirname(os.path.abspath(ini_path))
    cp = read_ini(ini_path)
    job = cp["job"] if cp.has_section("job") else {}

    def rel(p):
        return p if os.path.isabs(p) else os.path.join(ini_dir, p)

    if not job.get("template"):
        raise SystemExit(f"{ini_path}: [job] template = <existing job export> is required")
    template = rel(job["template"])
    key_in = key_override or job.get("template_key") or default_key()
    key_out = key_override or job.get("key") or default_key()
    root = ET.fromstring(decrypt_file(template, key_in))
    panel = root.find("Panel")
    rep = Report()

    panel_sec = dict(cp["panel"]) if cp.has_section("panel") else {}
    apply_panel(panel, panel_sec, rep)
    if cp.has_section("comm") or panel_sec.get("account"):
        apply_comm(panel, dict(cp["comm"]) if cp.has_section("comm") else {}, panel_sec, rep)
    if cp.has_section("outputs"):
        apply_outputs(panel, dict(cp["outputs"]), rep)

    zones = []
    if job.get("zones"):
        zones += read_zone_file(rel(job["zones"]))
    if cp.has_section("zones"):
        for k, v in cp["zones"].items():
            if not k.strip().isdigit():
                raise SystemExit(f"[zones] '{k}' is not a zone number")
            zones.append(parse_zone_line(int(k), v, f"[zones] {k}"))
    if zones:
        apply_zones(panel, zones, rep)
    else:
        rep.warn("no zones given - zones were left as they are in the template")

    name = get(panel.find("Account"), "NAME")
    out = rel(job["output"]) if job.get("output") else os.path.join(ini_dir, f"{name}.xml")
    if os.path.abspath(out) == os.path.abspath(template):
        raise SystemExit("output would overwrite the template - set a different [job] output")
    write_job(out, ET.tostring(root, encoding="unicode"), key_out)
    return out, rep


# ---------------------------------------------------------------- dump

def dump(path, key):
    panel = ET.fromstring(decrypt_file(path, key)).find("Panel")
    a = panel.find("Account")
    print(f"Panel: {get(a, 'NAME')}   account {get(a, 'ACCOUNT_NUM')}   receiver {get(a, 'RECVR_NUM')}")
    print(f"  Customer {get(a, 'CUSTOMER')}   Region {get(a, 'REGION')}")
    print(f"  {get(a, 'ADDRESS')}, {get(a, 'CITY')}, {get(a, 'STATE')} {get(a, 'ZIP')}")
    print(f"  IP {get(a, 'PANEL_IP')}:{get(a, 'PANEL_IP_PRT')}   phone {get(a, 'PANEL_PHONE')}   "
          f"serial {get(a, 'SERIAL_NUMBER')}   MAC {get(a, 'MAC_ADDRESS')}")
    for c in panel.findall("CommPathList/CommPath"):
        print(f"Comm path {get(c, 'NUMBER')}: type {get(c, 'PATH_TYPE')} comm {get(c, 'COMM_TYPE')} "
              f"account {get(c, 'ACCT_NUM')} phones {get(c, 'FIRST_PHON')}/{get(c, 'SEC_PHONE')} "
              f"test {get(c, 'TEST_TIME')[11:16]} delay {get(c, 'TRN_DELAY')}")
    print(f"Latched supervisory: {get(panel.find('SysOpts'), 'LATCH_SV')}")
    for o in panel.findall("OutNamesList/OutNames"):
        print(f"Output {get(o, 'NUMBER')}: {get(o, 'NAME')}")
    print("Zones:")
    for z in panel.findall("ZoneInfoList/ZoneInfo"):
        t = get(z, "TYPE")
        out = get(z, "AS_OUT_NUM")
        print(f"  {get(z, 'NUMBER'):>3} {get(z, 'NAME'):<32} {TYPE_NAMES.get(t, t):<12} "
              f"AO {MSG_NAMES.get(get(z, 'AO_ACT_MSG'), get(z, 'AO_ACT_MSG')):<8} "
              f"AS {MSG_NAMES.get(get(z, 'AS_ACT_MSG'), get(z, 'AS_ACT_MSG')):<8}"
              + (f" AS output {out}/{get(z, 'AS_OUT_ACT')}" if out != "0" else ""))


# ---------------------------------------------------------------- CLI

def main(argv=None):
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    b = sub.add_parser("build", help="build a job file from job.ini")
    b.add_argument("ini")
    for name in ("dump", "decrypt"):
        p = sub.add_parser(name)
        p.add_argument("job")
    sub.choices["decrypt"].add_argument("-o", "--output", required=True)
    e = sub.add_parser("encrypt")
    e.add_argument("xml")
    e.add_argument("-o", "--output", required=True)
    for p in sub.choices.values():
        p.add_argument("--key", help="export encryption key (default: settings.ini or DMP_EXPORT_KEY)")
    args = ap.parse_args(argv)
    key = args.key or (None if args.cmd == "build" else default_key())

    if args.cmd == "build":
        out, rep = build(args.ini, args.key)
        print("\n".join(rep.changes))
        for w in rep.warnings:
            print(f"WARNING: {w}")
        print(f"\nWrote {out}\nImport it in Remote Link, then check Panel Information, "
              f"Communication Paths and Zone Information before sending.")
    elif args.cmd == "dump":
        dump(args.job, key)
    elif args.cmd == "decrypt":
        with open(args.output, "w", encoding="latin1", newline="") as f:
            f.write(decrypt_file(args.job, key))
        print(f"Wrote {args.output}")
    elif args.cmd == "encrypt":
        text = open(args.xml, encoding="latin1").read()
        ET.fromstring(text)                     # refuse to write broken XML
        write_job(args.output, text, key)
        print(f"Wrote {args.output}")


if __name__ == "__main__":
    main()
