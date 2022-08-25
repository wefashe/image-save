-- drop table wallpaper if exists;
create table if not exists wallpaper
(
    startdate     varchar(8)            default ' ',
    fullstartdate varchar(50)           default ' ',
    enddate       varchar(8)   not null default ' ' comment '日期',
    url           varchar(150) not null default ' ' comment '壁纸地址',
    urlbase       varchar(100)          default ' ',
    copyright     varchar(150)          default ' ',
    copyrightlink varchar(150)          default ' ',
    title         varchar(100)  not null default ' ' comment '标题',
    quiz          varchar(150)          default ' ',
    hsh           varchar(50)           default ' ' comment '壁纸hash值',
    createtime    timestamp    not null default current_timestamp comment '创建时间',
    updatetime    timestamp    not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (enddate)
) comment = '壁纸信息表';