# Individual Analysis: selectTab

I picked selectTab(direction) in src/actions/action-handlers.ts (lines 20-54). It is behind five shortcuts: next tab, previous tab, first tab, last tab and go to tab N. 

```
async function selectTab(direction: string): Promise<void> {
  const tabs = await browser.tabs.query({ currentWindow: true })                          // 21
  if (tabs.length <= 1) return                                                            // 22
  const [currentTab] = await browser.tabs.query({ currentWindow: true, active: true })    // 24
  const currentIndex = tabs.findIndex((t) => t.id === currentTab.id)                      // 25
  let target
  switch (direction) {
    case 'next':     target = tabs[(currentIndex + 1) % tabs.length]; break               // 30
    case 'previous': target = tabs[(currentIndex - 1 + tabs.length) % tabs.length]; break // 33
    case 'first':    target = tabs[0]; break
    case 'last':     target = tabs[tabs.length - 1]; break                                // 39
    default: {
      const index = parseInt(direction) || 0                                              // 42
      if (index >= 1 && index <= tabs.length) target = tabs[index - 1]                    // 43
    }
  }
  if (target?.id) await browser.tabs.update(target.id, { active: true })                  // 49-50
}
```



I ran Stryker only on this method, using stryker.selecttab.config.json (before) and stryker.selecttab.after.config.json (after). Each run takes about 10 seconds. The configs, the reports and the test changes (selectTab-tests.patch) are in shortkeys-individual/.

## Overall result

Stryker made 50 mutants for this method.

| | Killed | Survived | No coverage | Score |
|---|---|---|---|---|
| Before | 10 | 19 | 21 | 10/50 = 20% |
| After my tests | 46 | 4 | 0 | 46/50 = 92% |

The 4 survivors left are equivalent (see below), so excluding them the score is 46/46.

The 21 uncovered mutants surprised me. The suite already has tests called handles action: firsttab, lasttab and gototabbyindex, but they all mock a window with a single tab, so selectTab returns at line 22 and the first/last/index code never runs.

## The 10 mutants

1. Next tab: currentIndex + 1 changed to currentIndex - 1. Killed before and after.
2. Previous tab: % tabs.length changed to * tabs.length. Killed before and after.
3. The guard tabs.length <= 1 changed to tabs.length > 1. Killed before and after.
4. The guard tabs.length <= 1 changed to tabs.length < 1. Survived before, killed after.
5. Previous tab: currentIndex - 1 changed to currentIndex + 1. Survived before, killed after.
6. Previous tab: + tabs.length changed to - tabs.length. Survived before, killed after.
7. Finding the current tab: t.id === currentTab.id changed to t.id !== currentTab.id. Survived before, killed after.
8. The first query: { currentWindow: true } changed to {}. Survived before, killed after.
9. Last tab: tabs.length - 1 changed to tabs.length + 1. No coverage before, killed after.
10. Go to tab N: index >= 1 changed to true. No coverage before, still survives after because it is equivalent.

Before: 3 killed, 7 live, score 3/10 = 30%. After: 9 killed, 1 equivalent, score 9/10 = 90% (9/9 without the equivalent one).

Mutants 1, 2 and 3 were already killed by the existing tests, so I left them as they were.

For mutants 5 and 7, I changed the existing next and previous tab tests to check which tab is activated, not just that one is.

For mutant 6, I added a test that starts on the first tab, goes to the previous tab, and expects the last tab.

For mutant 8, I added a test that checks the arguments passed to tabs.query.

For mutant 9, I added a test with three tabs that expects firsttab to pick tab 1 and lasttab to pick tab 3.

For mutant 4, I added a test that a window with one tab gets no tabs.update call.

Mutant 10 is equivalent. For an index below 1, tabs[index - 1] is undefined in JavaScript, so nothing happens, which is the same as the original. I added tests for positions 0, 4 and abc, and it still survives.

## Other unkilled mutants and duplicates

Three other mutants on line 43 are equivalent for the same reason: index <= tabs.length changed to true, the whole condition changed to true, and && changed to ||. Line 49 already ignores a missing target, so the range check on line 43 is redundant.

The mutant that changes index >= 1 to index > 1 is not equivalent, because it breaks go to tab 1. I added position 1 to the tests to kill it.

Mutant 4 and the mutant that replaces the same guard with false are duplicates, since a window always has at least one tab. The four equivalent mutants on line 43 are also duplicates of each other.

## Challenges

The hardest part was deciding equivalence, which needed reasoning about negative array indexes, parseInt and the sign of %. Some mutants, like mutant 6, only fail on one specific input, and I missed the index > 1 mutant the first time.

## Conclusion

The method is correct, but its tests were weak. They checked that a tab was activated but not which one, used a single tab that never got past the guard, and used mocks that ignore their arguments. With 9 new tests and 2 stronger ones, the score went from 20% to 92%, and the only survivors left are equivalent.
