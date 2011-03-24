/*
token types:
0 unspecified
1 author
2 meeting
3 source
4 editor 
*/

/* here we get the vitro entites that are cornell faculty */
/* as indicated by vivo.entities.typeid = 31 */
/* also netid's are joined in from the vivo.externalids table */
insert into calsdump.person 
    (id, entityId, name, moniker, netid, typeId, description, imageThumb, modtime)
    select e.id, e.id, e.name, e.moniker, l.value , e.typeid, e.description,
        e.imageThumb, null  
    from vivo3.entities as e, vivo3.externalids as l  
    where e.id = l.entityId 
    and l.externalidtype = '101'
    and e.typeid = 31;

insert into calsdump.linkTypes
   (id, linkType, generic, modtime)
    select id, type, generic, null
    from vivo3.linktypes;

/* here we get the links from the vivo.links table. */
/* but only for the entities that are in the calsdump.people table */
insert into calsdump.links
    (id, entityId, url, anchor, modtime, typeId )
    select id, entityid, url, anchor, null, typeid
    from vivo3.links
    where 
    vivo3.links.entityid in (select entityid from calsdump.person);

/* here we get the links that are in the entities table */
/* and give the the linktype of '13' */
insert into calsdump.links 
    (id, entityId, URL, anchor, modtime, typeid)
select null, id, url, anchor, null, 13
    from vivo3.entities 
    where 
    id in (select entityid from calsdump.person) 
    and vivo3.entities.url is not null;

/* We could pull in values from vivo3.links but there are duplicates */
/* for Human Resource's codes.  We colapse the redundency with the */
/* group by clause at the end.  */
insert into calsdump.department 
    (id, entityId, typeId, name, ADW_code, HR_code )
    select e.id, e.id, e.typeid, e.name,null, null
    from vivo3.entities as e left outer join vivo3.externalids as l  
    on e.id = l.entityId and l.externalidtype = 202
    where 
    e.typeid in (61 )
    group by e.id;

/******************** keyterms ********************/   
insert into calsdump.keyterms 
    (id, term,stem, modtime, typeSet, descriptorId, sourceSet)
    select id, term, stem, modtime, typeSet, descriptorId, sourceSet 
    from vivo3.keyterms k;

insert into calsdump.person_has_keyterms
    (person_id, keyterms_id )
    select entId as person_id, keyId as keyterms_id 
    from vivo3.keys2ents k, calsdump.person p
    where k.entId = p.entityId;

/******************** grants ********************/
/* get all the grants */
/* grants are etype=117 */
/* osp award id externalidtype = 401 */
insert into calsdump.grant_award
    (id, ospid, entityId, title, sunrise, sunset )
    select e.id, ex.value, e.id, e.name, e.sunrise, e.sunrise 
    from 
    vivo3.entities e, vivo3.externalids as ex
    where 
    e.id = ex.entityId
    and ex.externalidtype = 401
    and e.typeid = 117;
    
/******************** properties ********************/

/* person has department: based on etypes2relationsid = 48 and */
/* calsdump already having the person and department. */
insert into calsdump.person_has_department
    (person_id, department_id )
    select  rangeid, domainid from vivo3.ents2ents where 
    etypes2relationsid = 48
    and rangeid in (select entityid from calsdump.person)
    and domainid in (select entityid from calsdump.department);

/* person_has_grant_award */
/* role of 559 = "primary investigator" 560 = "co-investigator" */
insert into calsdump.person_has_grant_award
    (person_id, grant_award_id, role )
    select domainid, rangeid, etypes2relationsid from vivo3.ents2ents where     
    domainid in (select entityid from calsdump.person)
    and rangeid in (select entityId from calsdump.grant_award);

/* departmet_has_grant_award */
/* etypes2relationsid 561 = " department administers grant" */
insert into calsdump.department_has_grant_award
    (department_id, grant_award_id )
    select domainid, rangeid from vivo3.ents2ents where     
    domainid in (select entityid from calsdump.department)
    and rangeid in (select entityId from calsdump.grant_award)
    and etypes2relationsid = 561;



/******************** journal - author - publication ********************/

/* we want all the journals we have */
insert into calsdump.journal
    (id, tokenId, title, modtime) 
    select id, id, token, modTime 
    from vivo3.tokens where typeid = 3 order by token;
        
/* get publications, but only 2005 */
/* t.typeid = 3 pulls in journal tokens */
insert into calsdump.publication
    (id, entityId, journal_id, title, pub_year, 
        full_text_link, modtime)
    select p.id, p.entityId, t2p.tokenid, p.title, p.pubYear, 
        p.fullTextLink, p.modTime
    from vivo3.pubs as p, 
        vivo3.tokens2pubs as t2p, 
        vivo3.tokens as t
    where p.entityId is not null
        and p.id = t2p.pubId
        and t2p.tokenid = t.id
        and t.typeid = 3
        and p.pubYear = 2005
    group by p.id;

/* get authors */
/* author.id is the same as vivo3.tokens.id */
/* group by tokens.id to get rid of duplicates arising from one
   author having multiple publications */
/* we are also joining in the netid's from externalids */
/* maybe we should get the name from the entity name if we have one? */
insert into calsdump.author
    (id, name, cornell, entityid, netid)
    select t.id, t.token, t.entityid > 0, t.entityid, e.value
    from vivo3.tokens t join vivo3.tokens2pubs t2p    
    on  t2p.tokenid = t.id left outer join 
    vivo3.externalids e on t.entityid = e.entityid
    where 
    t2p.pubid in ( select distinct(id) from calsdump.publication )    
    and t.typeid = 1
    group by t.id;    

/* person_has_publication */
insert into calsdump.person_has_publication 
(publication_id, person_id)
    select pub.id, per.id
    from vivo3.ents2ents e2e, 
    calsdump.publication pub,
    calsdump.person per
     where     
     e2e.rangeid = per.entityid
     and e2e.domainid = pub.entityid
    and e2e.domainid in (select entityid from calsdump.publication )
    and e2e.rangeid in (select entityId from calsdump.person )
    and e2e.etypes2relationsid = 360;


/* publication_has_author */
-- insert into calsdump.person_has_publication 
-- (publication_id, person_id)
--     select p.id as 'pub id' , a.id as 'author token id'
--     from vivo3.tokens2pubs t2p, 
--     vivo3.tokens t,
--     calsdump.publication p,
--     calsdump.author a
--      where     
--      t2p.pubid = p.id
--      and t2p.tokenid = t.id
--      and t.typeid = 1
--      and t2p.tokenid = a.id
--     and t2p.pubid in (select id from calsdump.publication )
--     and t2p.tokenid in (select id  from calsdump.author)

/* publication_has_author */
insert into calsdump.publication_has_author
    (publication_id, author_id)
    select t2p.pubid as 'pub id' , t2p.tokenid as 'author token id'
    from vivo3.tokens2pubs t2p    
     where     
        t2p.pubid in (select id from calsdump.publication )
        and t2p.tokenid in (select id  from calsdump.author)
