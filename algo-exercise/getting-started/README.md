# Coding Black Females - My Trading Algorithm Application

### Overview
This project is a Java- based trading algorithm designed to automate decision making in the financial markets. The algorithm does this by analysing the order and executing buy and sell actions based on market conditions. The project aims to simplify trading operations by automatically managing orders, this helps users to make timely trading decisions without manual intervention. The main problem it addresses is the need for efficient and rule-based order management in fast-moving markets. It reduces human error by automating repetitive tasks like matching buy and sell orders, creating new orders based on specific conditions, and cancelling orders that no longer meet the criteria. This type of automation is particularly valuable for traders who want to capitalise on market opportunities quickly while adhering to a pre-defined strategy.

This project is useful because it enhances trading efficiency by using predefined thresholds and actions to guide decision-making. The algorithm’s logging system also provides real-time insights into its operations, which helps in monitoring performance and refining trading strategies. My project offers an extendable framework starting point for developers and traders interested in creating or optimising algorithmic trading solutions.

### How to Get Started

#### Pre-requisites

1. The project requires Java version 17 or higher

##### Note
This project is configured for Java 17. If you have a later version installed, it will compile and run successfully, but you may see warnings in the log like this, which you can safely ignore:

```sh
[WARNING] system modules path not set in conjunction with -source 17
```

#### Opening the project

1. Fork this repo in GitHub and clone it to your local machine
2. Open the project as a Maven project in your IDE (normally by opening the top level pom.xml file)
3. Click to expand the "getting-started" module

##### Note
You will first need to run the Maven `install` task to make sure the binary encoders and decoders are installed and available for use. You can use the provided Maven wrapper or an installed instance of Maven, either in the command line or from the IDE integration.

To get started, run the following command from the project root: `./mvnw clean install`. Once you've done this, you can compile or test specific projects using the `--projects` flag, e.g.:

- Clean all projects: `./mvnw clean`
- Test all `algo-exercise` projects: `./mvnw test --projects algo-exercise`
- Compile the `getting-started` project only: `./mvnw compile --projects algo-exercise/getting-started`
- Then run the Algotest/ AlgoBacktest to see the output of my code

 ☺️ 💻






