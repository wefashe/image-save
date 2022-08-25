-- drop table wallpaper if exists;
create table if not exists wallpaper
(
    startdate     varchar(8) not null,
    fullstartdate varchar(50) not null,
    enddate       varchar(8) not null,
    url           varchar(150) not null comment '壁纸地址',
    urlbase       varchar(100) not null,
    copyright     varchar(100) not null,
    copyrightlink varchar(150) not null,
    title         varchar(50) not null comment '标题',
    quiz          varchar(150) not null,
    hsh           varchar(50) not null comment '壁纸hash值',
    createtime    timestamp not null default current_timestamp comment '创建时间',
    primary key (hsh)
) COMMENT='必应壁纸信息';