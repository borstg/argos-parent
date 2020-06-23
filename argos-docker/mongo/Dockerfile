#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM mongo
RUN echo "rs.initiate({'_id':'rs0','members':[{'_id':0,'host':'127.0.0.1:27017'}]});" > /docker-entrypoint-initdb.d/replica-init.js
RUN cat /docker-entrypoint-initdb.d/replica-init.js
CMD [ "--bind_ip_all", "--replSet", "rs0" ]