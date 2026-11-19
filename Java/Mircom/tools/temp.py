import pandas as pd

# load files
unknown = pd.read_excel('unknown accounts.xlsx', engine='openpyxl')
rapid = pd.read_excel('rapid accounts.xlsx', engine='openpyxl')

# normalize column names
unk_col = unknown.columns[0]
rap_col = [c for c in rapid.columns if 'Account Number' in c][0]

# clean values as string
unknown_set = set(unknown[unk_col].astype(str).str.strip())
rapid['Account Number_str'] = rapid[rap_col].astype(str).str.strip()

# matches
match_df = rapid[rapid['Account Number_str'].isin(unknown_set)].copy()

# non matches
non_match_vals = unknown[~unknown[unk_col].astype(str).str.strip().isin(rapid['Account Number_str'])]
non_match_df = pd.DataFrame({unk_col: non_match_vals[unk_col]})

# write excel
output_file = 'account_comparison.xlsx'
with pd.ExcelWriter(output_file, engine='openpyxl') as writer:
    match_df.drop(columns=['Account Number_str']).to_excel(writer, sheet_name='Matches', index=False)
    non_match_df.to_excel(writer, sheet_name='Not in Rapid', index=False)

output_file