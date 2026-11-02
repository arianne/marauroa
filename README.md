# Group Analysis and Results
## Identification of the open-source project.
We are testing the [Maraurora](https://arianne-project.org/engine/marauroa.html) project. It is a multiplayer engine written in Java using a TCP oriented network protocol and an SQL based persistence engine. The project spans 26,000 lines of code and builds in 5 seconds from source code using `ant`. 
## Test suite infrastructure
Maraurora uses `JUnit` for its suite of 143 tests, across 2648 lines of code and takes 7 seconds to execute on a dry run, 4 seconds on subsequent runs.
## Results

| Metric | Numerator / denominator | Result |
|---|---|---|
| Line coverage | 3979 / 12624 | **32%** |
| Mutation score (killed + timeout) | 1533 / 6288 | **24%** |

## The output coverage reports that provide clear evidence of usage
![report](mutation-results\PIT-report.png)
## How do your results from PIT and Stryker compare?
TODO
## What do the results tell you about your test suite?
**DRAFT**

The report shows that the most tested aspects of the project are `marauora.common.[game\net]` because they have the most amount of coverage.
## Which system did you find easier to use (both integrate and interpret) and why?
TODO
## Do the test suites exhibit weaknesses? How can they be improved?
Database and network tests are weak
## Do the test suites exhibit strengths? How do you recognize them?
TODO
## Contrast the costs and benefits of mutation analysis and testing versus what you might expect from other techniques.
**Costs**: Mutation testing with PIT took 1 minute 10 seconds for the target excluding the database tests and 88 seconds on the target with the database and connection tests. 
**Benefits** : idk
## What was easy and what was difficult about applying the tools to the projects you chose?
The easy part was compiling and testing the project by itself. The hard part was getting PIT to work on our machines. Since Marauora uses `ant` to build, we had to get the correct `pitest` jars from the GitHub repo. WHich meant we needed to compile `pittest` itself and move the jars to the `libs/` directory in Marauora. Then we had to downgrade some libraries used by Marauora after reading all the error logs. 
## What obstacles did you face in applying mutation analysis to a real world project, and how did you overcome them?
PIT expected a a newer version of JUnit while our project was on an older release. Some of the database tests did not pass because the schema was not initialized during the tests. This is what claude told me but we should rewrite it in our own words:
> PIT runs all 260 test classes in one minion JVM, and static singletons like TransactionPool and DAORegister carry state between them. Ant’s <junit> with forkmode="once" does the same, but PIT runs tests in a different order and may run them more than once, so the collisions only show up there.
The way we solved this issue was making a separate PIT target specifically for the database tests. This approach yielded another set of results and its own report.
## Did you have any other interesting insights or opinions on the experience?
Each of us can say whatever ig. <br>
IMO (Viktor) it was interesting running the tests on this project which is an online game server, so as a user I wouldn't suspect how many interactions between components make the multiplayer game run. The fact that the mutation coverage is low may be a sign that the tests haven't been updated too often or they didn't take this approach into consideration when they were writing them.
**We should add our raw opinion first and then make it sound "academic" later**

# Individual Analysis and Results
Choose a complex routine that you think is likely to have errors. If fewer than 10 mutants were generated for the method, then either select a different method or select additional methods until you have 10 mutants to consider.

Examine 10 of the generated mutants for your method(s). If both killed and unkilled mutants were generated, include a mix of both. For each one, document your mutation operator. What was the type of operator used? How was it applied to the code (how did the code change)?

For your individual mutants consider the following additional questions: 
- How many mutants are killed?
- How many mutants are live? 
- For every mutant that was not killed, try to determine either 
    - (a) that it is an equivalent mutant that should not be killed
    - (b) how to add a test to kill it. 

Note that (b) involves a test case for a function with inputs and an oracle. Ideally, it is even written in the same testing framework as the project. 

For all mutants, try to determine whether they are duplicates of each other. 
- What are the challenges involved?
- Does it affect the results?
- Calculate and report the mutation score / effectiveness for your particular mutants. 

What do these results say about the effectiveness of the test suite and the method(s) that you considered?

## Oliver Analysis
TODO
## Melody Analysis
TODO
## Viktor Analysis
TODO