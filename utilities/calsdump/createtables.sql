CREATE TABLE `people` (
  `id` int(11) NOT NULL auto_increment,
  `entityId` int(11),
  `name` varchar(255) default NULL,
  `moniker` varchar(90) default NULL,
  `typeId` int(11) NOT NULL default '0',
  `description` text,
  `imageThumb` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
);

