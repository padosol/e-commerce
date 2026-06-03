-- DB per service. 로컬에선 단일 Postgres 컨테이너에 서비스별 DB를 분리 생성한다.
CREATE DATABASE auth;
CREATE DATABASE member;
CREATE DATABASE product;
CREATE DATABASE "order";
CREATE DATABASE payment;
CREATE DATABASE inventory;
CREATE DATABASE shipping;
