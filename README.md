# Temperature Sensor API

#### Assumptions:
```aidl
 1. The sensor exists and has a valid id -> id is not validated
 2. The sensor sends data per minute
 3. The sensor can send duplicated information -> but the information should be equal 
```

#### Use cases:

```
 Sensor -> PUT:/sensor/{sensorId}/temperature:sends one or many temperature data -> MongoDB:bulk upsert
        <-201

 User -> GET:/sensor/{sensorId}/temperature/daily:get aggregated daily temperature -> MongoDB:aggregate
      <- 200:aggregated data per day
      
 User -> GET:/sensor/{sensorId}/temperature/hourly:get aggregated hourly temperature -> MongoDB:aggregate
      <- 200:aggregated data per hour 

```

### Requirements
```
 Java 8
 Docker
 Postman 
```
***
### Using Make file to interact with this repo:
#### How to start

`make local`

It will start a dockerized app at localhost:8080 with mongoDB

#### How to stop

`make stop`

#### How to test

* Unit tests: `make unit-test`
* Integration tests: `make integration-test` (runs against a dockerized mongoDB instance)

**in case you don't have make please check the commands at ./makefile

***

### Test with postman:

Import the collection `./Temperature Sensor Api.postman_collection.json` in Postman and use the following request to interact with the API:
* Add Temperature Data
* Get Aggregated Daily Temperature
* Get Aggregated Hourly Temperature

***
##### Next steps:
* Add openApi documentation/swagger
* Increase test cases for web layer
* Improve exception handler at web layer
* Add security to mongoDB/web layer

#### Open questions:

- Is mongoDB aggregation the best solution? 
    - What would happen with high concurrency?
    - Is aggregation on request the best solution?
    - Evaluate moving the aggregation load from mongo to a data processing tool to save cpu/memory usage from mongo services
    - Evaluate removing the aggregation on request
      - Save aggregated information to avoid re aggregate on each request
- If consistency can be eventual, evaluate reading from secondary instances

***

MongoDb aggregate

Using compounded index on `date` and `sensorId`

daily:

```json
db.sensorData.aggregate([
  {
    "$match": {
      "sensorId": "922a3a6e-0fcf-49d8-9ea1-8ddceaa84b1e",
      "$and": [
        {
          "date": {
            "$gte": {
              "$date": "2021-10-29T22:00:00Z"
            }
          }
        },
        {
          "date": {
            "$lte": {
              "$date": "2021-10-31T22:59:59.999Z"
            }
          }
        }
      ]
    }
  },
  {
    "$addFields": {
      "yearMonthDay": {
        "$dateToString": {
          "format": "%Y-%m-%d",
          "date": "$date",
          "timezone": "Europe/Warsaw"
        }
      }
    }
  },
  {
    "$unwind": "$data"
  },
  {
    "$group": {
      "_id": "$yearMonthDay",
      "average": {
        "$avg": "$data.temperature"
      }
    }
  },
  {
    "$sort": {
      "_id": 1
    }
  },
  {
    "$addFields": {
      "date": {
        "$dateFromString": {
          "dateString": "$_id"
        }
      }
    }
  },
  {
    "$project": {
      "date": 1,
      "average": 1,
      "_id": 0
    }
  }
]
```

hourly:

```json
db.sensorData.aggregate([
  {
    "$match": {
      "sensorId": "922a3a6e-0fcf-49d8-9ea1-8ddceaa84b1e",
      "$and": [
        {
          "date": {
            "$gte": ISODate(
            "2021-10-29T22:00:00Z"
            )
          }
        },
        {
          "date": {
            "$lte": ISODate(
            "2021-11-01T00:00:00Z"
            )
          }
        }
      ]
    }
  },
  {
    "$addFields": {
      "yearMonthDayHour": {
        "$dateToString": {
          "format": "%Y-%m-%dT%H:00:00",
          "date": "$date",
          "timezone": "Europe/Warsaw"
        }
      }
    }
  },
  {
    "$unwind": "$data"
  },
  {
    "$group": {
      "_id": "$yearMonthDayHour",
      "average": {
        "$avg": "$data.temperature"
      }
    }
  },
  {
    "$sort": {
      "_id": 1
    }
  },
  {
    "$addFields": {
      "date": {
        "$dateFromString": {
          "dateString": "$_id",
          "timezone": "Europe/Warsaw"
        }
      }
    }
  },
  {
    "$project": {
      "date": 1,
      "average": 1,
      "_id": 0
    }
  }
])
```