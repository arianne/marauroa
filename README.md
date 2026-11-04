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
![PIT Report](mutation-results/PIT-report.png)
## How do your results from PIT and Stryker compare?
TODO
## What do the results tell you about your test suite?
**DRAFT**

The report shows that the most tested aspects of the project are `marauora.common.[game\net]` because they have the most amount of coverage.
## Which system did you find easier to use (both integrate and interpret) and why?
(melody) Stryker, setup is confined within the project. It doesn't need additional dependencies on the machine unlike with PIT where you need OpenJDK etc. Stryker is project specific
## Do the test suites exhibit weaknesses? How can they be improved?
The overall PIT report shows 32% line coverage and a 24% mutation score, so many paths are either untested or insufficiently checked. The database package's separate report located in `mutation-results-db/index.html` is somewhat better at 46% line coverage, but its mutation score is only 22% (141 of 651 mutants), indicating that many behavioral changes still go undetected. The networking results are also uneven. Although `marauroa.common.net` has 73% line coverage and a 58% mutation score, the client and server network packages have 0% line and mutation coverage in the main report.

These gaps could be improved by adding deterministic database integration tests such as tests to verify persistence, transaction, and error-handling behavior. For networking, it could be improved by adding focused tests for client/server connection lifecycles, timeouts, disconnects, malformed input etc. Across both areas, the improved tests should assert observable outcomes rather than only execute code.
## Do the test suites exhibit strengths? How do you recognize them?
The strengths of the test suite can be seen in `marauroa.common.game` with 80% line coverage, 58% mutation coverage and a test strength of 71%. Its 58% mutation coverage also shows that the tests detect many meaningful behavioral changes. Another strong area is a part of the network code in `marauroa.common.net`, with 73% line coverage, 60% mutation score, and 80% test strength. In its class breakdown, `Encoder` and `EncoderDecoderTest` each show 100% line and mutation coverage, suggesting the encoder behavior and its tested round-trip cases are checked thoroughly. 

These strengths are easy to recognize with the strong amount of coverage by the tests conducted, compared to the rest of the test coverage report, where most of the line and mutation coverage is at 0%.
## Contrast the costs and benefits of mutation analysis and testing versus what you might expect from other techniques.
**Costs**: Mutation testing with PIT took about 1 minute 10 seconds for the run excluding the database tests and 88 seconds for the run including the database and connection tests. This is more time than a normal test run ex. `ant test` because PIT repeatedly modifies the program and runs tests against those modified versions. The reports also take time to interpret as surviving mutants need reviewing since they can indicate missing assertions and also be equivalent to the original code.

**Benefits**: Mutation testing provides more insight as it reveals whether tests detect small behavioral changes as compared to line coverage which only shows whether some code ran. Additionally, compared with a regular JUnit run, PIT helps identify tests that pass without checking outcomes effectively. Its results also help prioritize areas for improvement such as showing stronger coverage in marauroa.common.game from other weaker less tested areas. 
## What was easy and what was difficult about applying the tools to the projects you chose?
The easy part was compiling and testing the project once we got PIT setup. The hard part was getting PIT to work on our machines. Since Marauora uses `ant` to build, we had to get the correct `pitest` jars from the GitHub repo. Which meant we needed to compile `pittest` itself and move the jars to the `libs/` directory in Marauora. Then we had to downgrade some libraries used by Marauora after reading all the error logs. We also encountered issues where the build for PIT testing would fail since we didn't have a compatible environment for it and it was difficult to locate the root cause. We had to go through different possibilities on what might be failing such as looking into incompatible Java versions or missing packages such as OpenJDK since the build failure messages were hard to interpret with the sheer amount of information printed out.
## What obstacles did you face in applying mutation analysis to a real world project, and how did you overcome them?
We encountered compatibility issues because the project used an older JUnit version than the one expected by our PIT setup. Additionally, some database tests also failed under PIT even though the regular test run worked. We discovered that the tests share a static singleton state, including `TransactionPool` and `DAORegister`, and PIT can run tests in a different order or rerun them while analyzing mutants. This shared state made the database tests sensitive to execution order. To work around this, we had to create a separate PIT target for the database tests and generated a dedicated report for them.
## Did you have any other interesting insights or opinions on the experience?
Each of us can say whatever ig. <br>
IMO (Viktor) it was interesting running the tests on this project which is an online game server, so as a user I wouldn't suspect how many interactions between components make the multiplayer game run. The fact that the mutation coverage is low may be a sign that the tests haven't been updated too often or they didn't take this approach into consideration when they were writing them.
**We should add our raw opinion first and then make it sound "academic" later**
(melody) it was interesting to see just how weak the test coverage for smaller areas of the program is with what looks like half being 0% line and mutation coverage. even though the main areas are well tested, it would definitely help with having some deterministic tests on networking and database as they are still an important area of the project overall

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
### Project: [chrome-shortkeys](https://github.com/Atune-D/cmpt473-a3-shortkeys)

**Method chosen:** `utils\url-matching.ts::normalizeUrlPattern` **analysed with Stryker**
```ts
export function normalizeUrlPattern(pattern: string): string {
  if (/^\/.+\/$/.test(pattern)) return pattern
  if (pattern.includes('*')) return pattern
  return `*${pattern}*`
}
```
**Original line:** 

`export function normalizeUrlPattern(pattern: string): string {function body}`

**Mutant 1:** 

`export function normalizeUrlPattern(pattern: string): string {}`
- BlockStatement
- Change: `function body` &rarr; `empty function body {}`
- Killed

**Original line:**

`if (/^\/.+\/$/.test(pattern)) return pattern`

**Mutant 2:**

 `if (true) return pattern`
- ConditionalExpression
- Change: `if (/^\/.+\/$/.test(pattern))` &rarr; `if (true)`
- Killed

**Mutant 3:** 

`if (false) return pattern;`
- ConditionalExpression
- Change: `if (/^\/.+\/$/.test(pattern))` &rarr; `if (false)`
- Killed

**Mutant 4:** 

`if (/^\/.\/$/.test(pattern)) return pattern`
- Regex
- Change: `/^\/.+\/$/` &rarr; `/^\/.\/$/`
- Killed

**Mutant 5:** 

`if (/\/.+\/$/.test(pattern)) return pattern`
- Regex
- Change: `/^\/.+\/$/` &rarr; `/\/.+\/$/`
- Survived

**Mutant 6:** 

`if (/^\/.+\//.test(pattern)) return pattern`
- Regex
- Change: `/^\/.+\/$/` &rarr; `/^\/.+\//`
- Survived

**Original line:**


`if (pattern.includes('*')) return pattern`

**Mutant 7:** 

`if (true) return pattern`
- ConditionalExpression
- Change: `if (pattern.includes('*'))` &rarr; `if (true)`
- Killed

**Mutant 8:** 

`if (false) return pattern`
- ConditionalExpression
- Change: `if (pattern.includes('*'))` &rarr; `if (false)`
- Killed

**Mutant 9:** 

`if (pattern.includes("")) return pattern`
- StringLiteral
- Change: `pattern.includes('*')` &rarr; `pattern.includes("")`
- Killed

**Original line:**


`return `*${pattern}*` `

**Mutant 10:**  

`return `` `
- StringLiteral
- Change:  `return `*${pattern}*` ` &rarr; `return `` `
- Killed

### How many mutants killed? How many mutants are live?
Out of the 10 chosen, 8 were killed and 2 survived.

### For every mutant that was not killed, determine either (a) that it is an equivalent mutant that should not be killed, or (b) how to add a test to kill it

Both of the surviving mutant are not equivalent mutants that should not be killed.

- **Mutant 5** doesn't check the start of the regular expression. It accepts and returns strings that end with `/` even when they do not start with `/`. In this case, adding a test such as `normalizeUrlPattern("prefix/a/")` will have the mutant return `prefix/a/`, instead of the expected `*prefix/a/*`, killing it.
- **Mutant 6** doesn't check the end of a regular expression. It accepts and returns a string that starts with a `/` even if it doesn't end with `/`. Adding a test such as `normalizeUrlPattern("/a/b")` will have the mutant return `/a/b` instead of the expected `*/a/b*`, killing it.

Since the project uses Vitest, these Vitest tests use the described inputs and expected output as the oracle:

```ts
describe('normalizeUrlPattern tests', () => {
  it('require start with slash if end with slash', () => {
    expect(normalizeUrlPattern('prefix/a/')).toBe('*prefix/a/*')
  })

  it('require end with slash if start with slash', () => {
    expect(normalizeUrlPattern('/a/b')).toBe('*/a/b*')
  })
})
```

The first test kills mutant 5, and the second kills mutant 6.

### For all mutants, try to determine whether they are duplicates of each other. What are the challenges involved? Does it affect the results?

Mutants **2, 7, and 9** are duplicates: each return `pattern` for every input. Mutant 9's `pattern.includes("")` condition is always considered true, just like the unconditional `true` conditions in mutants 2 and 7. They can all be killed by the same tests.

A challenge to determining duplicates is comparing outputs over the entire relevant input domain, not just comparing the mutant itself. For this function, the three duplicate mutants can be grouped together as they all have the same behaviour as `if (true)` at the same point in time at the function. More complex functions may require more careful path analysis or equivalence checking. When duplicates are found, it does affect the results, such as the end mutation score. When calculating the mutation score, you'll need to subtract the duplicates, however, if none are found then the mutation score can be calculated as 'normal'.

### Calculate and report the mutation score / effectiveness for your particular mutants. What do these results say about the effectiveness of the test suite and the method(s) that you considered?

The mutation score for the 10 selected mutants without counting in the duplicates is **8 / 10 = 80%**. If the duplicates (mutants 2, 7, and 9) are counted as one mutant, then the new mutation score is: 6 are killed and 2 survive, for a score of **6 / 8 = 75%**.

The tests detect most of the selected changes, but they miss two important regex-boundary cases. Adding the two test cases from above would improve the coverage of those boundaries. However, these results are only limited to this selected set of mutants for one method and doesn't represent the effectiveness of the projects entire test suite.


## Viktor Analysis
TODO