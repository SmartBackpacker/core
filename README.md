Smart Backpacker
================

`Smart Backpacker` is an App where you can find Visa Requirements information for any country in the world regardless of your nationality, currency exchange and airline's baggage policy!

- Download it for `Android` [here](https://play.google.com/store/apps/details?id=io.github.gvolpe.sb).
- Download it for `iOS` here - NOT AVAILABLE YET

### Modules

#### Api

It's the main back-end application exposing the Http Rest API and handling the database connection.

- Dependencies
    - [Fixer.io](http://fixer.io/) running on [localhost:8081](http://localhost:8081) using `docker-compose`. See the source code [here](https://github.com/hakanensari/fixer).

#### Airlines

It contains the [PostgreSQL](https://www.postgresql.org/) setup scripts for the airline tables and the job to insert new airline's data.

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with
the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.