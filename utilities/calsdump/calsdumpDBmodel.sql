CREATE TABLE links (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  entityId INTEGER UNSIGNED NULL,
  URL VARCHAR(255) NULL,
  anchor VARCHAR(255) NULL,
  modtime DATE NULL,
  typeId INTEGER(11) UNSIGNED NULL,
  PRIMARY KEY(id)
);

-- ------------------------------------------------------------
-- These are the keyterms or keywords.
-- 
-- There is a chance that the SET data type might cause some problems between
-- mysql and MS sql Server.
-- ------------------------------------------------------------

CREATE TABLE keyterms (
  id INTEGER UNSIGNED NOT NULL,
  term VARCHAR(255) NULL DEFAULT null,
  stem VARCHAR(245) NULL DEFAULT null,
  modtime DATE NULL,
  typeSet SET('keyword','collab','priority','controlled','curated','other') NULL DEFAULT null,
  descriptorId INTEGER(11) UNSIGNED NULL DEFAULT null,
  sourceSet SET('CALS','CUL','MeSH','NAL','Agrovoc') NULL DEFAULT null,
  PRIMARY KEY(id)
);

-- ------------------------------------------------------------
-- These are cornell faculty that we have in the vivo system.
-- 
-- name should be in the format 'lastname, firstname initial.'
-- 
-- moniker should be cornell faculty unless the person has a 
-- titled appointment.
-- 
-- typeid is the vivo.etype.id and is unlikely to be of much use to CALS rep.
-- 
-- description is a long description of the person, usually we put an excerpt from the
-- faculty's web page here, about 1 - 2 paragraphs.
-- 
-- image thumb is a url to a image on the vivo site.
-- ------------------------------------------------------------

CREATE TABLE person (
  id INTEGER(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  entityId INTEGER(11) UNSIGNED NULL,
  name VARCHAR(255) NULL,
  moniker VARCHAR(255) NULL,
  netid VARCHAR(25) NULL,
  typeId INTEGER(11) UNSIGNED NULL,
  description TEXT NULL,
  imageThumb VARCHAR(255) NULL,
  modtime DATE NULL,
  PRIMARY KEY(id)
);

CREATE TABLE LinkTypes (
  id INTEGER UNSIGNED NOT NULL,
  linktype VARCHAR(50) NOT NULL,
  generic VARCHAR(25) NULL,
  modtime DATE NULL
);

CREATE TABLE journal (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  tokenId INTEGER UNSIGNED NULL,
  title VARCHAR(255) NULL,
  abbreviation VARCHAR(50) NULL,
  modtime TIMESTAMP NULL,
  PRIMARY KEY(id)
);

CREATE TABLE department (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  entityId INTEGER UNSIGNED NULL,
  typeId INTEGER UNSIGNED NULL,
  name VARCHAR(255) NULL,
  ADW_code CHAR(8) NULL,
  HR_code VARCHAR(20) NULL,
  PRIMARY KEY(id)
);

CREATE TABLE author (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NULL,
  netid VARCHAR(25) NULL,
  cornell BOOL NULL,
  entityid INTEGER(11) UNSIGNED NULL,
  PRIMARY KEY(id)
);

-- ------------------------------------------------------------
-- We get all of the grant info from the Office of Sponsored Projects (OSP).
-- They have a data warehouse that we import from.  
-- ------------------------------------------------------------

CREATE TABLE grant_award (
  id INTEGER(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  ospId INTEGER UNSIGNED NULL,
  entityId INTEGER(11) UNSIGNED NULL,
  title VARCHAR(255) NULL,
  sunrise DATE NULL,
  sunset DATE NULL,
  modtime DATE NULL,
  PRIMARY KEY(id)
);

-- ------------------------------------------------------------
-- The following fields are not populated by the vivo system and may be populated by CALS Reporting:
-- volume, issue, pages, publisher, location, conference, isbn 
-- 
-- ------------------------------------------------------------

CREATE TABLE publication (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  journal_id INTEGER UNSIGNED NULL,
  entityId INTEGER(11) UNSIGNED NULL,
  title VARCHAR(255) NULL,
  pub_year INT NULL,
  volume VARCHAR(25) NULL,
  issue VARCHAR(25) NULL,
  pages VARCHAR(15) NULL,
  full_text_link VARCHAR(255) NULL,
  publisher VARCHAR(255) NULL,
  location VARCHAR(255) NULL,
  conference VARCHAR(255) NULL,
  isbn VARCHAR(13) NULL,
  modtime TIMESTAMP NULL,
  all_source VARCHAR(255) NULL,
  PRIMARY KEY(id),
  INDEX publication_FKIndex1(journal_id),
  FOREIGN KEY(journal_id)
    REFERENCES journal(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE person_has_publication (
  person_id INTEGER(11) UNSIGNED NOT NULL,
  publication_id INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(person_id, publication_id),
  INDEX person_has_publication_FKIndex1(person_id),
  INDEX person_has_publication_FKIndex2(publication_id),
  FOREIGN KEY(person_id)
    REFERENCES person(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(publication_id)
    REFERENCES publication(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE publication_has_author (
  publication_id INTEGER UNSIGNED NOT NULL,
  author_id INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(publication_id, author_id),
  INDEX publication_has_author_FKIndex1(publication_id),
  INDEX publication_has_author_FKIndex2(author_id),
  FOREIGN KEY(publication_id)
    REFERENCES publication(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(author_id)
    REFERENCES author(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE person_has_keyterms (
  person_id INTEGER(11) UNSIGNED NOT NULL,
  keyterms_id INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(person_id, keyterms_id),
  INDEX person_has_keyterms_FKIndex1(person_id),
  INDEX person_has_keyterms_FKIndex2(keyterms_id),
  FOREIGN KEY(person_id)
    REFERENCES person(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(keyterms_id)
    REFERENCES keyterms(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE department_has_grant_award (
  department_id INTEGER UNSIGNED NOT NULL,
  grant_award_id INTEGER(11) UNSIGNED NOT NULL,
  PRIMARY KEY(department_id, grant_award_id),
  INDEX department_has_grant_award_FKIndex1(department_id),
  INDEX department_has_grant_award_FKIndex2(grant_award_id),
  FOREIGN KEY(department_id)
    REFERENCES department(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(grant_award_id)
    REFERENCES grant_award(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE person_has_department (
  person_id INTEGER(11) UNSIGNED NOT NULL,
  department_id INTEGER UNSIGNED NOT NULL,
  role INTEGER UNSIGNED NULL,
  PRIMARY KEY(person_id, department_id),
  INDEX person_has_department_FKIndex1(person_id),
  INDEX person_has_department_FKIndex2(department_id),
  FOREIGN KEY(person_id)
    REFERENCES person(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(department_id)
    REFERENCES department(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE person_has_grant_award (
  person_id INTEGER(11) UNSIGNED NOT NULL,
  grant_award_id INTEGER(11) UNSIGNED NOT NULL,
  role INTEGER UNSIGNED NULL,
  PRIMARY KEY(person_id, grant_award_id),
  INDEX person_has_grant_award_FKIndex1(person_id),
  INDEX person_has_grant_award_FKIndex2(grant_award_id),
  FOREIGN KEY(person_id)
    REFERENCES person(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(grant_award_id)
    REFERENCES grant_award(id)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);


