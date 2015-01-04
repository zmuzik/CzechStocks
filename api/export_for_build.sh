#!/bin/bash
sqlite3 data.db 'select * from dividend;' > ../app/src/main/assets/dividend.csv
sqlite3 data.db 'select * from historical_quote;' > ../app/src/main/assets/historical_quote.csv
sqlite3 data.db 'select * from stock_detail;' > ../app/src/main/assets/stock_detail.csv
