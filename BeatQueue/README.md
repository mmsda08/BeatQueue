# BeatQueue

A song request and playlist manager for events and campus radio.
Plain Java + Swing. No libraries, no Maven, no Gradle. Needs Java 11 or newer (tested on Java 21).

## Run it in Eclipse

Option A - import the project (easiest)
1. Unzip BeatQueue.zip.
2. Eclipse: File > Import > General > Existing Projects into Workspace > Next.
3. Browse to the unzipped BeatQueue folder > Finish.
4. Open src/Main.java > right-click > Run As > Java Application.

Option B - new project
1. File > New > Java Project, name it BeatQueue, Finish.
2. Copy everything inside the zip's src folder into the project's src folder.
3. Refresh (F5), then run src/Main.java.

If Eclipse shows a JRE error, right-click the project > Properties > Java Build Path > Libraries
and set the JRE System Library to any Java 11+ JRE.

## Run the self-test
Right-click src/tests/SelfTest.java > Run As > Java Application. It prints PASS or FAIL for every data structure.

## Screens
- Library: search with trie autocomplete, sort (merge sort), Exact Title Lookup (binary search), Request Song.
- Queue: requests in first-come order, Vote +1, Play Next (most votes, earliest wins ties).
- Playlist: Previous, Next, Play Selected, Insert Song, Delete Selected, Shuffle.
- Top 10: bar chart of the most voted songs.
- Admin: add songs (duplicates rejected), recent actions (newest first).
- Undo button (top right): reverses the newest action.

## Where each data structure / algorithm lives
| Requirement | File |
|---|---|
| Queue (first-come, first-served) | src/ds/RequestQueue.java |
| Priority queue (max-heap, earlier request wins ties) | src/ds/VotePriorityQueue.java |
| Doubly linked list (playlist) | src/ds/Playlist.java |
| Hash table (song ID, request ID, already-requested check) | src/ds/HashTable.java (used in controller/BeatQueueController.java) |
| Trie (autocomplete on any word of title or artist) | src/ds/Trie.java |
| Merge sort (title, artist, votes) | src/algo/MergeSort.java |
| Fisher-Yates shuffle | src/algo/FisherYates.java |
| Stack (undo, recent actions) | src/ds/ActionStack.java |
| Top-K heap (Top 10 chart) | src/ds/TopKHeap.java |
| Binary search (exact title, duplicate songs) | src/algo/BinarySearch.java |

## Change the look
All colors, fonts and the corner radius are in src/ui/Theme.java.

## Manual test checklist
- Search "dua" -> 2 songs. Search "queen" -> 2 songs (Dancing Queen, and Bohemian Rhapsody by Queen). Search "dua lev" -> 1 song.
- Request a song, then request it again -> error message (hash table).
- Request 3 songs, vote for the 2nd one twice, press Play Next -> the 2nd song plays first.
- Request 2 songs with equal votes, Play Next -> the earlier request plays first.
- Playlist: insert at position 1, delete, next, previous, shuffle. Press Undo after each.
- Library sort by Votes -> most voted first. Top 10 shows the same order.
- Admin: add "Levitating" by "Dua Lipa" -> rejected as a duplicate.
- Exact Title Lookup: type "Perfect" -> 1 match.
