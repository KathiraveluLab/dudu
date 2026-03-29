# ∂u∂u 
Distributed Near Duplicate Detection for Big Data

## Prerequisites
- Java 8 or higher
- Maven 3.6+
- Docker & Docker Compose (optional, for databases)

## Getting Started

### 1. Start Infrastructure (Docker)
To start the required MongoDB and MySQL databases:
```bash
docker-compose up -d
```

### 2. Build and Run
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="org.dudu.Main"
```


## Citing ∂u∂u

If you use ∂u∂u in your research, please cite the below paper:

* Kathiravelu, P., Galhardas, H. and Veiga, L., 2015, October. **∂u∂u Multi-Tenanted Framework: Distributed Near Duplicate Detection for Big Data.** In OTM Confederated International Conferences" On the Move to Meaningful Internet Systems" (pp. 237-256). Cham: Springer International Publishing.
