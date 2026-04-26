# ∂u∂u 
Distributed Near Duplicate Detection for Big Data

## Prerequisites
- Java 8 or higher
- Maven 3.6+
- Docker & Docker Compose (optional, for databases)

## Getting Started

### 1. Start Infrastructure
 We provide a dynamic setup script that handles infrastructure detection (Host vs. Docker) and builds the project.

```bash
chmod +x setup.sh
./setup.sh
```

The script will automatically decide whether to start Docker containers or use your host-level database instances.

### 2. Run
By default, the framework runs on the sample `name` dataset.
```bash
mvn exec:java -Dexec.mainClass="org.dudu.Main"
```

To run against a specific file and blocking key:
```bash
mvn exec:java -Dexec.mainClass="org.dudu.Main" -Dexec.args="data/records.csv product"
```

## Sample Data
A sample dataset is provided in `data/records.csv`. It contains records for both `name` and `product` blocking keys to demonstrate multi-tenanted detection.

### Configuration
The application and the startup script use environment variables for database configuration. If not set, they default to standard ports:
- `DUDU_MONGO_PORT`: Port for MongoDB (Default: 27017 or 27018 on conflict)
- `DUDU_MYSQL_PORT`: Port for MySQL (Default: 3306 or 3307 on conflict)

---

## Citing ∂u∂u

If you use ∂u∂u in your research, please cite the below paper:

* Kathiravelu, P., Galhardas, H. and Veiga, L., 2015, October. **∂u∂u Multi-Tenanted Framework: Distributed Near Duplicate Detection for Big Data.** In OTM Confederated International Conferences" On the Move to Meaningful Internet Systems" (pp. 237-256). Cham: Springer International Publishing.
