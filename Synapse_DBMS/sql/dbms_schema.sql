create table venue (
   venue_id serial primary key,
   venue_name varchar(255) not null,
   capacity int not null check (capacity > 0)
);

create table accommodation (
   room_id serial primary key,
   capacity int not null check (capacity > 0),
   status varchar(50) not null check (status in ('available', 'occupied', 'maintenance'))
);

create table departmentdirectory (
   dept_id serial primary key,
   dept_name varchar(100) not null unique,
   contact_person varchar(100),
   on_call_number varchar(20),
   reports_to_dept_id int
);

create table vendors (
   vendor_id serial primary key,
   vendor_name varchar(255) not null,
   contact varchar(255),
   type varchar(100)
);

create table sponsors (
   sponsor_id serial primary key,
   sponsor_name varchar(255) not null,
   contact varchar(255),
   tier varchar(50) check (tier in ('title', 'cotitle', 'platinum', 'associate')),
   contribution_amount numeric(15, 2) check (contribution_amount >= 0)
);

create table merchandise (
   merch_id serial primary key,
   item_name varchar(255) not null,
   description text,
   price numeric(15, 2) not null check (price >= 0),
   quantity_available int not null check (quantity_available >= 0)
);

create table events (
   event_id serial primary key,
   event_name varchar(255) not null,
   event_description text,
   event_type varchar(100) not null,
   schedule timestamp not null,
   prize_money numeric(15, 2) check (prize_money >= 0),
   venue_id int not null,
   constraint fk_events_venue
       foreign key (venue_id) references venue(event_id) on delete restrict
);

create table attendees (
   attendee_id serial primary key,
   name varchar(255) not null,
   phone_number varchar(20) unique,
   room_id int,
   constraint fk_attendees_accommodation
       foreign key (room_id) references accommodation(room_id) on delete set null
);

create table participants (
   participant_id serial primary key,
   attendee_id int not null unique,
   is_outstation boolean not null default false,
   arrival_date date,
   team_name varchar(255),
   constraint fk_participants_attendees
       foreign key (attendee_id) references attendees(attendee_id) on delete cascade
);

create table judgesartists (
   person_id serial primary key,
   name varchar(255) not null,
   contact varchar(255),
   arrival_date date,
   role varchar(50) not null check (role in ('judge', 'artist')),
   fee numeric(15, 2) check (fee >= 0),
   assignment_role varchar(255),
   event_id int,
   constraint fk_judges_events
       foreign key (event_id) references events(event_id) on delete set null
);

create table ticketing (
   ticket_id serial primary key,
   ticket_type varchar(50) not null check (ticket_type in ('vip', 'general')),
   price numeric(15, 2) not null check (price >= 0),
   status varchar(50) not null check (status in ('sold', 'available', 'cancelled')),
   purchase_date date,
   attendee_id int,
   constraint fk_ticketing_attendees
       foreign key (attendee_id) references attendees(attendee_id) on delete set null
);

create table transaction (
   transaction_id serial primary key,
   type varchar(100) not null check (type in ('sponsorship', 'vendor payment', 'ticket sale', 'merchandise sale')),
   amount numeric(15, 2) not null,
   timestamp timestamp not null default current_timestamp,
   description text,
   sponsor_id int,
   vendor_id int,
   ticket_id int,
   merch_id int,
   constraint fk_transaction_sponsor
       foreign key (sponsor_id) references sponsors(sponsor_id),
   constraint fk_transaction_vendor
       foreign key (vendor_id) references vendors(vendor_id),
   constraint fk_transaction_ticketing
       foreign key (ticket_id) references ticketing(ticket_id),
   constraint fk_transaction_merchandise
       foreign key (merch_id) references merchandise(merch_id),
   constraint chk_transaction_source
       check (
           (case when sponsor_id is not null then 1 else 0 end) +
           (case when vendor_id is not null then 1 else 0 end) +
           (case when ticket_id is not null then 1 else 0 end) +
           (case when merch_id is not null then 1 else 0 end)
           = 1
       )
);

create table volunteers (
   volunteer_id serial primary key,
   name varchar(255) not null,
   contact varchar(255),
   dept_id int not null,
   constraint fk_volunteers_department
       foreign key (dept_id) references departmentdirectory(dept_id) on delete restrict
);

create table inventory (
   item_id serial primary key,
   item_name varchar(255) not null,
   type varchar(100),
   quantity int not null check (quantity >= 0),
   condition text,
   dept_id int not null,
   constraint fk_inventory_department
       foreign key (dept_id) references departmentdirectory(dept_id) on delete cascade
);

create table registration (
   participant_id int not null,
   event_id int not null,
   registration_date date not null default current_date,
   primary key (participant_id, event_id),
   constraint fk_registration_participants
       foreign key (participant_id) references participants(participant_id) on delete cascade,
   constraint fk_registration_events
       foreign key (event_id) references events(event_id) on delete cascade
);

create table judgesevaluation (
   person_id int not null,
   participant_id int not null,
   event_id int not null,
   score numeric(5, 2) check (score >= 0 and score <= 100),
   primary key (person_id, participant_id, event_id),
   constraint fk_evaluation_judges
       foreign key (person_id) references judgesartists(person_id) on delete cascade,
   constraint fk_evaluation_participants
       foreign key (participant_id) references participants(participant_id) on delete cascade,
   constraint fk_evaluation_events
       foreign key (event_id) references events(event_id) on delete cascade
);

create table eventmanagement (
   volunteer_id int not null,
   event_id int not null,
   primary key (volunteer_id, event_id),
   constraint fk_management_volunteers
       foreign key (volunteer_id) references volunteers(volunteer_id) on delete cascade,
   constraint fk_management_events
       foreign key (event_id) references events(event_id) on delete cascade
);

alter table departmentdirectory
add constraint fk_dept_reports_to
foreign key (reports_to_dept_id)
references departmentdirectory(dept_id)
on delete set null;
