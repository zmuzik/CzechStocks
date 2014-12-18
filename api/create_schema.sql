CREATE TABLE current_data ("isin" text primary key, "price" real, "delta" real, "stamp" text);
CREATE TABLE stock_info ("isin" text, "indicator" text, "value" text);
CREATE TABLE historical_data ("isin" text, "stamp" integer, "price" real, "volume" real);
CREATE TABLE todays_data ("isin" text, "stamp" integer, "price" real, "volume" real);
CREATE TABLE dividend ("isin" text, "amount" real, "currency" text, "ex_date" integer, "payment_date" integer);
