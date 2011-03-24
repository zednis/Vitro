delete from ents2data where entityid in (select id from entities where vclassid = 245);
delete from ents2ents  where rangeid in (select id from entities where vclassid = 245);
delete from ents2ents where domainid in (select id from entities where vclassid = 245);
delete from externalids where entityid in (select id from entities where vclassid = 245);
delete from externalids where externalidtype = 401;
delete from links where entityid in (select id from entities where vclassid = 245);
delete from entities where vclassid = 245;

