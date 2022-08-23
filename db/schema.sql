create table wallpaper
(
    startdate     VARCHAR(8) NOT NULL,
    fullstartdate VARCHAR(50) NOT NULL,
    enddate       VARCHAR(8) NOT NULL,
    url           VARCHAR(150) NOT NULL comment '壁纸地址',
    urlbase       VARCHAR(100) NOT NULL,
    copyright     VARCHAR(100) NOT NULL,
    copyrightlink VARCHAR(150) NOT NULL,
    title         VARCHAR(50) NOT NULL comment '标题',
    quiz          VARCHAR(150) NOT NULL,
    hsh           VARCHAR(50) NOT NULL comment '壁纸HASH值',
    desc          text NOT NULL,
    PRIMARY KEY (hsh)
);