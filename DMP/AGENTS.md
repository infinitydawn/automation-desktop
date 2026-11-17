This is for DMP programming via Remote Link. The other files outside of the DMP folder in this repository are irrelevant.


## dmp_job.py — build Remote Link job exports
- Remote Link "DMP Panel Information (*.xml)" exports are hex of AES-128-ECB; key = MD5(export encryption key). The team key lives in `DMP/settings.ini` (`[dmp] key = ...`, gitignored) or the DMP_EXPORT_KEY env var - never put it in tracked files.
- `build_job.bat "jobs\<job>.ini"` (or `python dmp_job.py build <ini>`) copies a template export and applies [panel], [comm], [outputs], [zones]. See `jobs/example.ini`.
- Zone type/messages come from `ZONE_RULES` (Device Cheat Sheet - Fire). Unknown names stop the build; add `| type=FI` or `| type=SV`.
- `python dmp_job.py dump <job.xml>` prints a readable summary; `decrypt`/`encrypt` convert to/from plain XML.
- Confirmed type codes: FI (Fire), SV (Supervisory). Messages: A (Alarm), T (Trouble). Output action S = Steady.
