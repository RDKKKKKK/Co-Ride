create table organization
(
    idOrganization   int auto_increment
        primary key,
    organizationName varchar(45) not null,
    emailSuffix      varchar(45) null
);

create table user
(
    idUser         int auto_increment
        primary key,
    accountNo      varchar(45) not null,
    password       varchar(45) not null,
    name           varchar(45) not null,
    idOrganization int         null,
    phone          varchar(45) null,
    constraint user_ibfk_1
        foreign key (idOrganization) references organization (idOrganization)
            on update cascade on delete cascade
);

create table address
(
    idAddress int auto_increment
        primary key,
    name      varchar(45) not null,
    longitude double      not null,
    latitude  double      not null,
    idUser    int         not null,
    constraint address_ibfk_1
        foreign key (idUser) references user (idUser)
);

create index idUser
    on address (idUser);

create table administrator
(
    idAdministrator int not null
        primary key,
    constraint administrator_ibfk_1
        foreign key (idAdministrator) references user (idUser)
);

create table carpooler
(
    idCarpooler int         not null
        primary key,
    profile     varchar(45) null,
    constraint carpooler_ibfk_1
        foreign key (idCarpooler) references user (idUser)
            on update cascade on delete cascade
);

create table driver
(
    idDriver int not null
        primary key,
    rating   int null,
    constraint driver_ibfk_1
        foreign key (idDriver) references user (idUser)
            on update cascade
);

create table carpool_group
(
    idCarpoolGroup       int auto_increment
        primary key,
    idDriver             int         not null,
    originLongitude      double      not null,
    originLatitude       double      not null,
    time                 datetime    not null,
    seatsAvailable       int         not null,
    destinationLongitude double      not null,
    destinationLatitude  double      not null,
    status               varchar(45) null,
    estimatedArrivalTime datetime    null,
    oiginPoint           point       null,
    destinationPoint     point       null,
    departureTime        datetime    null,
    plateNo              varchar(45) null,
    originName           varchar(45) null,
    destinationName      varchar(45) null,
    constraint carpool_group_ibfk_1
        foreign key (idDriver) references driver (idDriver)
);

create index idDriver
    on carpool_group (idDriver);

create table carpool_request
(
    idCarpoolRequest     int auto_increment
        primary key,
    idCarpooler          int         not null,
    originLongitude      double      not null,
    originLatitude       double      not null,
    time                 datetime    not null,
    destinationLongitude double      not null,
    destinationLatitude  double      not null,
    idCarpoolGroup       int         null,
    status               varchar(45) null,
    originName           varchar(45) null,
    destinationName      varchar(45) null,
    originPoint          point       not null,
    destinationPoint     point       not null,
    departureTime        datetime    null,
    idOrganization       int         null,
    constraint carpool_request_ibfk_1
        foreign key (idCarpooler) references carpooler (idCarpooler),
    constraint carpool_request_ibfk_2
        foreign key (idCarpoolGroup) references carpool_group (idCarpoolGroup)
);

create index idCarpoolGroup
    on carpool_request (idCarpoolGroup);

create index idCarpooler
    on carpool_request (idCarpooler);

create spatial index idx_destination
    on carpool_request (destinationPoint);

create spatial index idx_origin
    on carpool_request (originPoint);

create index idx_accountNo
    on user (accountNo);

create table vehicle
(
    idVehicle int auto_increment
        primary key,
    seats     int         not null,
    name      varchar(45) not null,
    plateNo   varchar(45) not null,
    constraint idx_plateNo
        unique (plateNo)
);

create table has_vehicle
(
    idHasVehicle int auto_increment
        primary key,
    idDriver     int null,
    idVehicle    int null,
    constraint has_vehicle_driver_idDriver_fk
        foreign key (idDriver) references driver (idDriver),
    constraint has_vehicle_vehicle_idVehicle_fk
        foreign key (idVehicle) references vehicle (idVehicle)
);


