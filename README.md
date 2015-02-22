Zauberstuhl
===========

This is the source from zauberstuhl.de!

Application secret
------------------

Please generate a new one:

    $ play-generate-secret
    Generated new secret: QCYtAnfkaZiwrNwnxIlR6CTfG3gf90Latabg5241ABR5W1uDFNIkn
    [success] Total time: 0 s, completed 28/03/2014 2:26:09 PM


Setup
-----

Install simple build tool (sbt) and switch to the project directory, run:

    $ sbt clean compile stage

Create sqlite database table:

    $ sqlite3 database.db "CREATE TABLE PAYPAL (payer_id INT, first_name VARCHAR, last_name VARCHAR, email VARCHAR, gross FLOAT, fee FLOAT, currency VARCHAR);"

Run the Application
-------------------

Run from the project directory:

    $ target/universal/stage/bin/zauberstuhl
