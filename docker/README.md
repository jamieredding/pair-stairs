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
  - It is built from [/backend/Dockerfile](/backend/Dockerfile) using `dockerfile-maven-plugin` in the [/backend/pom.xml](/backend/pom.xml)
- pair-stairs-frontend
  - This container runs the frontend server (nginx) for pair-stairs
    - It serves both the static files and proxies API requests to the backend server
  - It is built from [/frontend/docker/Dockerfile](/frontend/docker/Dockerfile) by the makefile target `build-frontend-image` in [/makefiles/frontend.mk](/makefiles/frontend.mk)

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
