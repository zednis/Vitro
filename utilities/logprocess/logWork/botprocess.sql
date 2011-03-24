--find robots by who looks at robots.txt
insert ignore into robot_agent 
    select null, agent from log 
    where request like '%robots.txt%'  
    group by agent;

-- update log set client_ip=concat('artificial-googlebot',right(client_ip,3)) where agent like 'Googlebot/%';
-- update log set client_ip=concat('artificial-googlebot-image',right(client_ip,3)) where agent like 'Googlebot-Image%';
-- update log set client_ip=concat('artificial-aipbot',right(client_ip,3)) where agent like 'aipbot%';
-- update log set client_ip=concat('artificial-webcollage',right(client_ip,3)) where agent like 'webcollage%';
-- update log set client_ip=concat('artificial-IRLbot',right(client_ip,3)) where agent like 'IRLbot%';
-- update log set client_ip=concat('artificial-NutchCVS',right(client_ip,3)) where agent like 'NutchCVS%';
-- update log set client_ip=concat('artificial-LinkWatcher',right(client_ip,3)) where agent like 'LinkWalker';
-- update log set client_ip=concat('artificial-ai_archiver',right(client_ip,3)) where agent like 'ai_archiver%';
-- update log set client_ip=concat('artificial-YahooSeeker',right(client_ip,3)) where agent like 'YahooSeeker%';
-- update log set client_ip=concat('artificial-msnbot',right(client_ip,3)) where agent like 'msnbot%';
-- update log set client_ip=concat('artificial-KnowItAll',right(client_ip,3)) where agent like 'KnowItAll%';
-- update log set client_ip=concat('artificial-AnswerBus',right(client_ip,3)) where agent like 'AnswerBus%';
-- update log set client_ip=concat('artificial-Missuga-locator',right(client_ip,3)) where agent like 'Missigua%';
-- update log set client_ip=concat('artificial-scirus-locator',right(client_ip,3)) where agent like 'FAST%scirus%';
