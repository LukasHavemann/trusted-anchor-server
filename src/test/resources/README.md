# Documentation

## Certifcate

openssl genrsa -des3 -out myCA.key 2048
openssl req -x509 -new -nodes -key myCA.key -sha256 -days 1825 -out myCA.pem

passwort: test
https://serverfault.com/questions/979094/openssl-keeps-creating-v1-certificate-instead-of-v3


## Loadtesting

ab -l -k -n 10000 -c 10 http://localhost:8080/signHash/SomeValue

### First Run single request

Benchmarking localhost (be patient)
Completed 1000 requests
Completed 2000 requests
Completed 3000 requests
Completed 4000 requests
Completed 5000 requests
Completed 6000 requests
Completed 7000 requests
Completed 8000 requests
Completed 9000 requests
Completed 10000 requests
Finished 10000 requests


Server Software:        
Server Hostname:        localhost
Server Port:            8080

Document Path:          /signHash/SomeValue
Document Length:        Variable

Concurrency Level:      10
Time taken for tests:   24.887 seconds
Complete requests:      10000
Failed requests:        0
Keep-Alive requests:    0
Total transferred:      8869873 bytes
HTML transferred:       8219873 bytes
Requests per second:    401.82 [#/sec] (mean)
Time per request:       24.887 [ms] (mean)
Time per request:       2.489 [ms] (mean, across all concurrent requests)
Transfer rate:          348.05 [Kbytes/sec] received

Connection Times (ms)
min  mean[+/-sd] median   max
Connect:        0    0   0.1      0       4
Processing:    10   24  19.6     21     351
Waiting:       10   24  19.3     21     350
Total:         10   25  19.6     21     351

Percentage of the requests served within a certain time (ms)
50%     21
66%     24
75%     25
80%     27
90%     33
95%     48
98%     90
99%    116
100%    351 (longest request)
### Second run Batching