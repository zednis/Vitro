-- this creates a graph for graphVis dot 
-- you have to get rid of the collumn headers
-- and run it through something like
-- $ mysql --skip-column-names < makeTabGraphs.sql > out.txt
-- dot -Tgif -f out.txt -o tabs.gif


select concat('digraph G {' )  from entities limit 1;
select concat(id, ' [label="', title, '"];')  from tabs;
select concat('p',id, ' [shape=box,label="', appName, '"];')  from portals;
select concat( 'p', portalid, ' -> ' , id , ';') from tabs where tabtypeid in (26 , 28);
select concat(broaderId, ' -> ', narrowerId, ';') from tabs2tabs;
select concat( '}' )  from entities limit 1;

