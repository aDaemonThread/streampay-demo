# StreamPay App Demo
This StreamPay app demo consists of 4 main components such as:

- Redpanda.
- Event processing service written using `Spring Boot`.
- Zilla API Gateway that hosts both app web interface and APIs.
- StreamPay app UI

## Requirements

* [Node.js](http://nodejs.org/)
* [Docker](https://www.docker.com/)


## Redpanda
Redpanda servers both as event streaming source and database table to store information about users, transactions,
and stats. Following topics will be created:
- `commands` - This topics get populated by Zilla API Gateway and responsible for processing commands
such as `PayCommand`, `RequestCommand`.
- `replies` - HTTP response for processed command should be posted to this topic for correlated response.
- `transactions` - Stores information about about each transaction between users.
- `activities` - Event sourcing topic that logs all the activities in the system between users.
- `balances` - Tracks latest balance of a user comping from transactions table.
- `payment-requests` - Store payments requested by the user.
- `users` - Stores information about users(logcompacted topic).

## Event Processing Service
This service responsible for processing commands such as `PayCommand`, `RequestCommand` and producing messages
to the appropriate topics. It also has statistic topologies that builds activities, statistics out of topics such as
`transactions`, and `payment-requests`

### Build the service
All components are launched from docker stack defined in `stack.yaml` however, streampay-service is reference to
`image: "streampay-service:develop-SNAPSHOT"` which should be build locally. Please run the below command to build image.

```shell
cd service
./mvnw clean install
cd ..
```
The above command will generate `streampay-service:develop-SNAPSHOT` image.

## StreamPay UI
This app is build using `Vue.js` and `Quasar` frameworks and contains user authentication component as well
which uses Auth0 platform.

### Build

```shell
cd app
npm i -g @quasar/cli
npm install
quasar build
cd ..
```

The above command will generate `dist` folder with all the necessary files to be hosted by Zilla API Gateway.

## Zilla API Gateway
Zilla API Gateway will be hosting both app UI and APIs. Following endpoints are configured in `zilla.jon`

| Protocol | Method | Endpoint          | Topic            |
|----------|--------|-------------------|------------------|
| SSE      | GET    | /activities       | activities       |
| SSE      | GET    | /payment-requests | payment-requests |
| SSE      | GET    | /current-balance  | balances         |
| HTTP     | POST   | /pay              | commands         |
| HTTP     | POST   | /request          | commands         |
| HTTP     | PUT    | /current-user     | users            |
| HTTP     | GET    | /current-user     | users            |
| HTTP     | GET    | /users            | users            |


## Launch the stack
Run following command to launch the stack:

```shell
cd stack
docker stack deploy -c stack.yml example --resolve-image never
```

```shell
#Output
Creating network example_net0
Creating service example_zilla
Creating service example_redpanda
Creating service example_init-redpanda
Creating service example_streampay-service
```

# Test

Navigate to `https://localhost:9090` in the browser.

![screenshot](./assets/screenshot.png)

Click on login and use one of the option to authenticate. Happy testing!


