create sequence hibernate_sequence start 5 increment 1;

create table employee (
    id int8 not null,
    department_id int8 not null,
    first_name varchar(255),
    last_name varchar(255),
    title varchar(255),
    primary key (id)
);

insert into employee (id, department_id, first_name, last_name, title)
values (1, 1, 'Maksim', 'Tikhonov', 'Lead IT Engineer'),
       (2, 1, 'Sergey', 'Ivanov', 'Senior IT Engineer'),
       (3, 2, 'Anna', 'Demeleva', 'QA Engineer'),
       (4, 3, 'Svetlana', 'Melnikova', 'Accounting specialist');