# securityservice

This application was generated using JHipster 6.2.0, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v6.2.0](https://www.jhipster.tech/documentation-archive/v6.2.0).

## Development

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

    npm install

We use npm scripts and [Webpack][] as our build system.

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    ./mvnw
    npm start

Npm is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `npm update` and `npm install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `npm help update`.

The `npm run` command will list all of the scripts available to run for this project.

### PWA Support

JHipster ships with PWA (Progressive Web App) support, and it's disabled by default. One of the main components of a PWA is a service worker.

The service worker initialization code is commented out by default. To enable it, uncomment the following code in `src/main/webapp/index.html`:

```html
<script>
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./service-worker.js').then(function() {
      console.log('Service Worker Registered');
    });
  }
</script>
```

Note: [Workbox](https://developers.google.com/web/tools/workbox/) powers JHipster's service worker. It dynamically generates the `service-worker.js` file.

### Managing dependencies

For example, to add [Leaflet][] library as a runtime dependency of your application, you would run following command:

    npm install --save --save-exact leaflet

To benefit from TypeScript type definitions from [DefinitelyTyped][] repository in development, you would run following command:

    npm install --save-dev --save-exact @types/leaflet

Then you would import the JS and CSS files specified in library's installation instructions so that [Webpack][] knows about them:
Note: There are still a few other things remaining to do for Leaflet that we won't detail here.

For further instructions on how to develop with JHipster, have a look at [Using JHipster in development][].

## Building for production

### Packaging as jar

To build the final jar and optimize the security application for production, run:

    ./mvnw -Pprod clean verify

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

    java -jar target/*.jar

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.

### Packaging as war

To package your application as a war in order to deploy it to an application server, run:

    ./mvnw -Pprod,war clean verify

## Testing

To launch your application's tests, run:

    ./mvnw verify

### Client tests

Unit tests are run by [Jest][] and written with [Jasmine][]. They're located in [src/test/javascript/](src/test/javascript/) and can be run with:

    npm test

For more information, refer to the [Running tests page][].

### Code quality

Sonar is used to analyse code quality. You can start a local Sonar server (accessible on http://localhost:9001) with:

```
docker-compose -f src/main/docker/sonar.yml up -d
```

You can run a Sonar analysis with using the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) or by using the maven plugin.

Then, run a Sonar analysis:

```
./mvnw -Pprod clean verify sonar:sonar
```

If you need to re-run the Sonar phase, please be sure to specify at least the `initialize` phase since Sonar properties are loaded from the sonar-project.properties file.

```
./mvnw initialize sonar:sonar
```

or

For more information, refer to the [Code quality page][].

## Using Docker to simplify development (optional)

You can use Docker to improve your JHipster development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    ./mvnw -Pprod verify jib:dockerBuild

Then run:

    docker-compose -f src/main/docker/app.yml up -d

For more information refer to [Using Docker and Docker-Compose][], this page also contains information on the docker-compose sub-generator (`jhipster docker-compose`), which is able to generate docker configurations for one or several JHipster applications.

## Continuous Integration (optional)

To configure CI for your project, run the ci-cd sub-generator (`jhipster ci-cd`), this will let you generate configuration files for a number of Continuous Integration systems. Consult the [Setting up Continuous Integration][] page for more information.

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 6.2.0 archive]: https://www.jhipster.tech/documentation-archive/v6.2.0
[using jhipster in development]: https://www.jhipster.tech/documentation-archive/v6.2.0/development/
[using docker and docker-compose]: https://www.jhipster.tech/documentation-archive/v6.2.0/docker-compose
[using jhipster in production]: https://www.jhipster.tech/documentation-archive/v6.2.0/production/
[running tests page]: https://www.jhipster.tech/documentation-archive/v6.2.0/running-tests/
[code quality page]: https://www.jhipster.tech/documentation-archive/v6.2.0/code-quality/
[setting up continuous integration]: https://www.jhipster.tech/documentation-archive/v6.2.0/setting-up-ci/
[node.js]: https://nodejs.org/
[yarn]: https://yarnpkg.org/
[webpack]: https://webpack.github.io/
[angular cli]: https://cli.angular.io/
[browsersync]: http://www.browsersync.io/
[jest]: https://facebook.github.io/jest/
[jasmine]: http://jasmine.github.io/2.0/introduction.html
[protractor]: https://angular.github.io/protractor/
[leaflet]: http://leafletjs.com/
[definitelytyped]: http://definitelytyped.org/

# securityservice

### What is this repository for? ###
Security service for applications. To use it in individual application, you can add this module
as dependency. You can configure the application access urls by using ``synectiks.shiro.secure.urls`` property.
In this property you can configuration security with role and permission access rule, Its a
multivalued property so you can specify comma separated list of these rules in ``application.properties`` file with following format:

	{"url"*: "/api/v1/auth", "authc"*: true, "roles": "role1, role2", "permissions": "permis1, permis2"}

In case you would like to exclude any url from secure path, add it into ``synectiks.shiro.public.urls`` property in following format:

	{"url"*: "/api/v1/public/**", "authc"*: false}
	
### Break points to identify the encrypted password string:

	AuthenticatingRealm [line: 600] - assertCredentialsMatch(AuthenticationToken, AuthenticationInfo)	
	DefaultPasswordService [line: 164] - passwordsMatch(Object, String)
	
### Init Queries from db schema

	insert into public.permission(id, name, permission) values(1, 'All', '*');
	insert into public.roles(id, name) values (2, 'ROLE_ADMIN');
	insert into public.users(id, active, password, type, username) values(3, true, '$shiro1$SHA-256$500000$imA8niWsVhzN5kanmIVtRQ==$urr3E3/PM52eG3QCHz3SjGN6huN0MIwJ2Kg22RBUnPg=', 2, 'admin');
	insert into public.roles_permissions values (2, 1);
	insert into public.users_roles values(3, 2);

### How to import project for editing ###

* Import as maven project in your IDE

### Build, install and run application ###

To get started build the build the latest sources with Maven 3 and Java 8 
(or higher). 

	$ cd security
	$ mvn clean install 

You can run this application as spring-boot app by following command:

	$ mvn spring-boot:run

Once done you can run the application by executing 

	$ java -jar target/security-exec.jar

### Configuration parameters for application run ###
	Key	       		Default Value
	---------------------------------------
	SERVER_PORT		8094
	PSQL_HOST	 		localhost
	PSQL_PORT	 		5432
	PSQL_DB   		synectiks
	PSQL_PSWD	 		xxxxxx

## Application api's documentation ##

### /security/public/login ###

Api to get authenticate user with username and password.

	Method: POST | GET
	Params:
		username*		String
		password*		String
		rememberMe	boolean
		redirectTo	String url to redirect on success
	Response:
		Success	authentication response

### /security/public/signup ###

Api to get authenticate user with LoginRequest object.

	Method: POST
	Params:
		request*	LoginRequest object
	Response:
		Success	authentication response

### /security/public/signin ###

Api to get authenticate user with User object.

	Method: POST
	Params:
		request*	User object
	Response:
		Success	authentication response

### /security/public/authenticate ###

Api to get authenticate user with UsernamePasswordToken object.

	Method: POST
	Params:
		token*		UsernamePasswordToken object
	Response:
		Success	authentication response

### /security/public/logout ###

Api to get authenticate user with UsernamePasswordToken object.

	Method: POST | GET
	Response:
		Success

### /security/permissions/listAll ###

Api to all permission objects.

	Method: POST | GET
	Response:
		{} Json list of Permission objects

### /security/permissions/create ###

Api to create new permission objects.

	Method: POST
	Params:
		service Json permission object
	Response:
		{} Json of Permission objects

### /security/permissions/{id} ###

Api to fetch permission objects by id.

	Method: POST
	Response:
		{} Json of Permission objects

### /security/permissions/delete/{id} ###

Api to delete a permission objects by id.

	Method: POST
	Response:
		Success message

### /security/permissions/update ###

Api to update a permission by json objects.

	Method: POST
	Params:
		entity Json permission object
	Response:
		{} Json of Permission objects

### /security/permissions/delete ###

Api to delete a permission by json object.

	Method: POST
	Params:
		entity Json permission object
	Response:
		Success

### /security/roles/listAll ###

Api to all Role objects.

	Method: POST | GET
	Response:
		{} Json list of Role objects

### /security/roles/create ###

Api to create new Role objects.

	Method: POST
	Params:
		service Json Role object
	Response:
		{} Json of Role objects

### /security/role/{id} ###

Api to fetch Role objects by id.

	Method: POST
	Response:
		{} Json of Role objects

### /security/roles/delete/{id} ###

Api to delete a Role objects by id.

	Method: POST
	Response:
		Success message

### /security/roles/update ###

Api to update a Role by json objects.

	Method: POST
	Params:
		entity Json Role object
	Response:
		{} Json of Role objects

### /security/roles/delete ###

Api to delete a Role by json object.

	Method: POST
	Params:
		entity Json Role object
	Response:
		Success

### /security/users/listAll ###

Api to all User objects.

	Method: POST | GET
	Response:
		{} Json list of User objects

### /security/users/create ###

Api to create new User objects.

	Method: POST
	Params:
		service Json User object
	Response:
		{} Json of User objects

### /security/users/{id} ###

Api to fetch User objects by id.

	Method: POST
	Response:
		{} Json of User objects

### /security/users/delete/{id} ###

Api to delete a User objects by id.

	Method: POST
	Response:
		Success message

### /security/users/update ###

Api to update a User by json objects.

	Method: POST
	Params:
		entity Json User object
	Response:
		{} Json of User objects

### /security/users/delete ###

Api to delete a User by json object.

	Method: POST
	Params:
		entity Json User object
	Response:
		Success

### Who do I talk to? ###
	Please mail us on
	info@syenctiks.com
