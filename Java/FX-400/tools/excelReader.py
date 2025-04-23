import pandas as pd

raw_df = pd.read_csv('../assets/data.csv', encoding='utf-8')
df = raw_df.sort_values(by = 'Zone', ascending=True)

# print(df)

print("Dataframe loaded and sorted by Zone")


def check_decimal(number):
    number_str = str(number)
    if "." not in number_str:
        return False
    if number_str.endswith(".1") or number_str.endswith(".2"):
        return True
    else:
        return False
    
#haha

def check_columns(df):
    required_columns = ['Zone', 'Type', 'Location']
    for column in required_columns:
        if column not in df.columns:
            errorMessage = f"Missing required column: {column}"
            raise ValueError(errorMessage)


# check if the columns are present in the dataframe
check_columns(df)

zoneColumn = df['Zone']
typeColumn = df['Type']
locationColumn = df['Location']

print(zoneColumn)

# check if the zone column is in correct format
for index, value in enumerate(zoneColumn):
    if(check_decimal(value) == False):
        errorMessage = f"Zone: {value}, row {index+1} is not in correct format, make sure it ends with .1 or .2"
        raise ValueError(errorMessage)

    # print(f"Index: {index}, Value: {value}")
    # print(f"correct format: {check_decimal(value)}")

print("Check if every .2 zone has a .1 zone...")
# check if every .2 zone has a .1 zone
for index, value in enumerate(zoneColumn):
    
    if(check_decimal(value) == True and str(value).endswith(".2")):
        # print(f"Zone: {value}, row {index+1} is in correct format")
        # print(f"this zone: {value}, previous zone: {zoneColumn[index-1]}")
        if(value - 0.1 not in zoneColumn):
            errorMessage = f"Zone: {value}, row {index+1} is .2 zone but previous zone: {zoneColumn[index-1]} is not .1 zone"
            raise ValueError(errorMessage)



