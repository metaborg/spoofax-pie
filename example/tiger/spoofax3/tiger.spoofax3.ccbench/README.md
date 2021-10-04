# Code Completion Benchmark

This performs a code completion benchmark test. The inputs are files of the test language (Tiger, WebDSL), with a placeholder somewhere in the file, and the cursor offset of the placeholder to be completed.

For each completion, it measures the time from invocation until getting results. We may or may not have to include the time required for parsing and initial analysis.
