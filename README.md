# Cell ID Decoder

This application is able to decode Cell IDs from hexadecimal values of the GSM MAP 
`subscriberInfo.locationInformation.cellGlobalIdOrServiceAreaIdOrLAI.cellGlobalIdOrServiceAreaIdFixedLength`
field.

It contains values that look like this as hex: `02f81027045275` and decodes them to their respective
MCC, MNC, LAC and Cell ID values. 

## Example usage: 

```
$ java -jar cellid-decoder-1.0.0.jar "02f81027045275"
MCC: 208, MNC: 001, LAC: 9988, Cell-ID: 21109
```

## Terminology:

MCC = Mobile country code: Identifies the country of the cell tower
MNC = Mobile network code: Identifies the operator of the mobile network within the country
LAC = Location area code: Identifies the location within the country
Cell ID = Unique ID within the LAC that identifies the cell tower

## How to build

```
mvn clean package
```

The output jar file will be in the "target" directory.