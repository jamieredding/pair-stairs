# Docker

There are two example stacks to use here, the main difference being how the backend data is stored.

- [h2](./h2/README.md)
  - The backend data is stored in a file that is managed in-memory by the backend container
  - This can be simpler to manage than a full database
- [mysql](./mysql/README.md)
  - The backend data is stored in a mysql database running in its own container
  - This will offer a more scalable solution but required more considered maintenance

## Primary containers

- pair-stairs-backend
  - This container runs the backend server for pair-stairs
    - Handling API requests from the frontend and persisting data to the database
    - Serving frontend as static content 
  - It is built from [/backend/Dockerfile](/backend/Dockerfile) using `dockerfile-maven-plugin` in the [/backend/pom.xml](/backend/pom.xml)

## Running the containers

Please see the relevant README.md ([h2](./h2/README.md), [mysql](./mysql/README.md)) for instructions and more detail.

## Data migration

### h2 to mysql

> [!NOTE]
> Please read all steps before trying to follow this.

1. Have an up to date h2 database running with the latest migrated schema (containing data you want to migrate)
2. Obtain a copy of the database file (`data.mv.db`)
   - Shut down the database
   - Copy it out of the volume
   - ```shell
     docker run --name h2_dump --rm -it -v h2_pair_stairs_h2:/tmp/test bash
     docker cp h2_dump:/tmp/test/data.mv.db .
     ```
3. Dump the database file (`data.mv.db`) to a sql file (`backup.sql`)
   - Against the `data.mv.db` file run the following sql
     - You can connect to the file using IntelliJ and an `embedded` h2 connection type
   - ```h2
     SCRIPT TO 'backup.sql'
     ```
4. Edit the dumped sql (`backup.sql`) to make it compatible for mysql (`restore.sql`)
   - Remove everything except the `INSERT` statements
   - Remove the inserts into the `flyway_schema_history` as they will already exist
   - Ensure all tables and schemas are correctly named
     - In IntelliJ, configure the file to use the mysql database as a data source to try and identify further errors
   - Ensure all data that you expect to be in the file is present
5. Have an up to date mysql database running with the latest migrated schema (this should be empty other than the flyway table)
6. Run the compatible sql file (`restore.sql`) against the mysql database
7. Ensure data looks valid in mysql
8. You shouldn't need to update the auto-increment on the tables as I have observed this not being necessary and mysql figuring it out
   - If it is required, consider something like the following to update the value
   - ```mysql
     select MAX(id) FROM pair_stairs.developers;
     alter table pair_stairs.developers AUTO_INCREMENT = ?;
     
     select MAX(id) FROM pair_stairs.streams;
     alter table pair_stairs.streams AUTO_INCREMENT = ?;
     
     select MAX(id) FROM pair_stairs.pair_streams;
     alter table pair_stairs.pair_streams AUTO_INCREMENT = ?;
     
     select MAX(id) FROM pair_stairs.combinations;
     alter table pair_stairs.combinations AUTO_INCREMENT = ?;
     
     select MAX(id) FROM pair_stairs.combination_events;
     alter table pair_stairs.combination_events AUTO_INCREMENT = ?;
     ``` 

### mysql to h2

> [!NOTE]
> Please read all steps before trying to follow this.

1. Have an up to date mysql database running with the latest migrated schema (containing data you want to migrate)
2. Dump the database (`backup.sql`)
    - You will need to have permissions to run mysqldump, likely root will have this
    - ```shell
      docker exec -it mysql-pair_stairs_db-1 bash
      mysqldump -u root -p pair_stairs --no-create-info > backup.sql
      ```
    - Then copy it out of the container
    - ```shell
      docker cp mysql-pair_stairs_db-1:/backup.sql .
      ```
3. Copy `backup.sql` to `restore.sql`
4. Edit the dumped sql (`restore.sql`) to make it compatible for mysql
    - Remove everything except the `INSERT` statements
    - Remove the inserts into the `flyway_schema_history` as they will already exist
    - Ensure all inserts are in a valid order
      - Likely:
        - developers
        - streams
        - pair_streams
        - developer_pair_member
        - combinations
        - combination_pair_member
        - combination_events
    - Ensure all tables and schemas are correctly named
        - In IntelliJ, configure the file to use the mysql database as a data source to try and identify further errors
        - Tables should be `PUBLIC.<table_name>` in h2
    - Ensure all data that you expect to be in the file is present
5. Have an up to date h2 database running with the latest migrated schema (this should be empty other than the flyway table)
6. Obtain a copy of the database file (`data.mv.db`)
    - Shut down the database
    - Copy it out of the volume
    - ```shell
      docker run --name h2_dump --rm -it -v h2_pair_stairs_h2:/tmp/test bash
      docker cp h2_dump:/tmp/test/data.mv.db .
      ```
7. Run the compatible sql file (`restore.sql`) against the h2 database
    - Against the `data.mv.db` file run the `restore.sql` file
        - You can connect to the file using IntelliJ and an `embedded` h2 connection type
8. Ensure data looks valid in h2
9. Update the auto-increment on the tables as I have observed this not working in h2
    - ```h2
      select MAX(id) FROM PUBLIC.developers;
      alter table PUBLIC.developers ALTER COLUMN id RESTART WITH ?;
      
      select MAX(id) FROM PUBLIC.streams;
      alter table PUBLIC.streams ALTER COLUMN id RESTART WITH ?;
      
      select MAX(id) FROM PUBLIC.pair_streams;
      alter table PUBLIC.pair_streams ALTER COLUMN id RESTART WITH ?;
      
      select MAX(id) FROM PUBLIC.combinations;
      alter table PUBLIC.combinations ALTER COLUMN id RESTART WITH ?;
      
      select MAX(id) FROM PUBLIC.combination_events;
      alter table PUBLIC.combination_events ALTER COLUMN id RESTART WITH ?;
      ``` 
10. Import the `data.mv.db` database back into volume
     - Copy it into the volume
     - ```shell
       docker run --name h2_dump --rm -it -v h2_pair_stairs_h2:/tmp/test bash
       docker cp data.mv.db h2_dump:/tmp/test/data.mv.db
       ```