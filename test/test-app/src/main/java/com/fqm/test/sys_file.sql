-- ----------------------------
-- 业务系统文件，同数据库
-- ----------------------------
DROP TABLE IF EXISTS sys_file;
CREATE TABLE sys_file  (
  file_id 		  	bigint         not null,						comment '文件主键（雪花算法）',
  business_id     	bigint         default 0 						comment '业务表主键（雪花算法）',
  business_source 	varchar(50)    default ''						comment '业务表来源(如：商品)',
  file_path       	varchar(255)   default '' 		  				comment '文件路径（如：a/2010/10/10/a_2020.png）',
  file_suffix 	  	varchar(10)    default '' 						comment '文件后缀名（如：doc）',
  file_name 	  	varchar(255)   default '' 						comment '文件名（如：a_2020.png）',
  file_size 	  	bigint 		   default 0 						comment '文件大小（byte）',
  file_type 	  	tinyint 	   default NULL 					comment '文件类型：1:图片 2:视频 3:文件',
  file_content_type varchar(20)    default '' 						comment '文件类型（如：image/png）',
  file_status		tinyint		   default 0						comment '文件状态：0:删除、1:使用、2：禁用',
  file_order		int		       default 0						comment '文件排序',
  file_group_id 	bigint 		   default 0 						comment '文件分组id（和字段file_group_name，2选1）',
  file_group_name 	varchar(50)    default 0 						comment '文件分组名称（如：主图、轮播图、附件，和字段file_group_id，2选1）',
  
  create_time     	datetime       default CURRENT_TIMESTAMP        comment '创建时间',
  create_by       	varchar(64)    default ''        				comment '创建人',
  update_time     	datetime       default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
  update_by       	varchar(64)    default ''        				comment '更新人',
  PRIMARY KEY (file_id)
) ENGINE = InnoDB comment = '文件记录表';
-- business_id + business_source + file_status +（file_group_name/file_group_id）联合索引

DROP TABLE IF EXISTS file_group;
CREATE TABLE file_group  (
  file_group_id		bigint         not null,						comment '文件分组主键（雪花算法）',
  parent_id			bigint         not null,						comment '父ID（file_group_id：雪花算法）',
  business_source 	varchar(64)    default ''						comment '业务表类型(如：商品)',
  name 				varchar(50)    default ''						comment '分组名称（如：主图、轮播图、附件）',
  create_time     	datetime       default CURRENT_TIMESTAMP        comment '创建时间',
  create_by       	varchar(64)    default ''        				comment '创建人',
  update_time     	datetime       default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
  update_by       	varchar(64)    default ''        				comment '更新人',
  PRIMARY KEY (file_group_id)
) ENGINE = InnoDB comment = '文件分组表';