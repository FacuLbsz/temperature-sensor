check:
	./gradlew check
test:
	./gradlew test
unit-test:
	./gradlew test --tests *.unit.*
integration-test:
	./gradlew test --tests *.integration.*
docker:
	./gradlew jibDockerBuild -Djib.to.tags=latest
up:
	docker-compose up -d
stop:
	docker-compose stop
local: docker up