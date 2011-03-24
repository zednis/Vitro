#code to generate impacts 2 dept ents2ents
#bdc34
select concat('insert into ents2ents (domainid, rangeid, propertyid) values (',ip.id, ',', depts.id, ',582);')
from entities ip 
join ents2ents submit on ip.id = submit.domainid
join entities submitter on submitter.id = submit.rangeid
join ents2ents deptrel on submitter.id = deptrel.rangeid
join entities depts on deptrel.domainid = depts.id
where ip.vclassid = 270 and ip.citation like '%2006'
and submit.propertyid = 566
and deptrel.propertyid = 506;