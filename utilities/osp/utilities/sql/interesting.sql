-- get all entities.typeid's for folks with external id's
select distinct (ent.typeid)  from entities ent , externalids ex where 
ent.id = ex.entityid  and ex.externalidtype in (101, 102);

-- Get all entities where there is no external id:
select ent.id, ent.name, ex.id from 
entities ent  left outer join externalids ex on ent.id = ex.entityid 
where 
ent.typeid in (90,32,94,31,33) and ex.id is null ;

--get staff names, their depts, the external id for the dept, and the dept entity id
select a.name  , b.name , externalids.value, externalids.externalidtype, b.id as deptentityid 
from entities a, entities b,  ents2ents, externalids  
where a.id=rangeid and b.id=domainid 
and etypes2relationsid= 117  and entityid=b.id and externalidtype in (201,202)  order by b.name

-- get investigator netid and name where there is external id in vivo 
select invproj_investigator_netid, invproj_full_name  from investigators_t inv 
where concat(invproj_investigator_netid, '@cornell.edu' ) 
not in ( select value from vivo3.externalids where externalidtype=101)

some commands I've been using:
source resetdb.sh vivo3-20050202.sql 
ant compile && java -cp "build;ChangeQueue.jar;commons-logging-api.jar;commons-logging.jar;ldap.jar" LdapNetidReader people_in_osp_not_in_vivo.txt 
ant compile && java -cp "build;ChangeQueue.jar;commons-logging-api.jar;commons-logging.jar;ldap.jar" LoadNetids entitiesSansExId.txt 
ant compile && java -cp "build;ChangeQueue.jar;commons-logging-api.jar;commons-logging.jar;ldap.jar" makeLdapSearchFilter people_in_osp_not_in_vivo.txt 
ant compile && java -cp "build;ChangeQueue.jar;commons-logging-api.jar;commons-logging.jar;ldap.jar" LdapNetidReader people_in_osp_not_in_vivo.txt 