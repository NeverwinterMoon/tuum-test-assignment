# Requirements
- Java 19 (JDK must be installed and $JAVA_HOME configured)
- Docker

# Running the application
`./gradlew bootRun`

# Testing
`./gradlew test`

Regarding test coverage, Jacoco reporting is included. The coverage is slightly under 80%, primarily due to Lombok annotations. Some of these annotations are only necessary within the scope of integration tests. Apart from this, services and controllers along with all possible logic are covered at a rate exceeding 80%.

# Development process

First and foremost, I thoroughly enjoyed executing this task.

My initial foray into programming was with Java (not considering my obsession with Visual Basic at school) and the Spring framework. Since then, I have diverged from Java backend work, moving towards Kotlin backends, web development in JavaScript, then Elm, and eventually onto Android and iOS native app development along with server development on Node.js. I received this task on June 6, 2023, and am submitting it on June 10, 2023. Regrettably, I couldn't allocate proper time to this task until June 8, 2023 due to work commitments and several sporting activities.

I've initilized the project with the help of Spring Initializr on June 6, but most of the code was written on June 8 and June 9, followed by writing tests on June 10 and refactoring much of the code.

The only challenge I encountered during this task was exploring every required technology stack: I haven't used Java and Spring for many years, have never used MyBatis, utilized Docker in a simpler way a long time ago (never used Docker Compose), never used RabbitMQ (but have experience with Google Pub/Sub), and never used PostgreSQL. Fortunately, all these technologies were not only easy to grasp (at least within the scope of this task) but also extremely captivating.

The most challenging part for me was using Java. My most recently used typed languages are Kotlin and Swift. Returning to Java felt a bit awkward, primarily due to the need for type names preceding variables and methods. I understand that this is subjective and habit-related, but I still believe that writing a name and then type is generally easier to process. Additionally, the mandatory use of semicolons felt archaic (neither Kotlin nor Swift require this). Another point is nullability. It is much cleaner and easier to handle in Kotlin.

Fundamentally, I am curious about why you chose to use Java when it's relatively simple to migrate to Kotlin (even incrementally). Not only is Kotlin more concise, but it also offers performance benefits in certain situations, as far as I'm aware.

Just to clarify, I'm merely expressing curiosity about this choice, and not being judgmental. Aside from the inconveniences mentioned above, I relished the mental challenge of working with a language I haven't used in years and which differs from the languages I've used recently. I am certainly looking forward to working with it more. Ultimately, it's not about the technology itself, but rather how the technology is utilized and the benefits it can provide.

# Stress testing
I've implemented a basic Gatling simulation to test the performance of my machine under different levels of simultaneous requests. My machine consistently handled up to 500 simultaneous connections (within a second) without any issues. However, when I exceeded that threshold, I noticed that Gatling started to receive some percentage of premature closures which were not due to timeouts. I haven't seen any warnings or errors neither on server side nor DB/RabbitMQ, so it might have been due to incorrect Gatling configuration (another tool I haven't used for a while).

If you decide to try out my specific Gatling implementation, please note that it requires a pre-existing account ID created elsewhere. To run the simulation, use the command `./gradlew gatlingRun`.

# Horizontal scaling
When attempting to scale the application horizontally, several factors need to be considered. The most crucial, perhaps, in the context of this application, is account balance processing. If multiple instances of the application try to process transactions and modify the same balance concurrently, it's likely that the balance data could be out of sync for one or more of these application instances.

Moreover, with regard to the question of estimating the stress load any machine can handle, it's vital to set up load balancing/auto-scaling between different application instances/machines to distribute the load evenly.
