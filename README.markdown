## PrimeNumbersCalculation

The app calculates prime numbers from some intervals specified in xml-based file placed in Asset folder.
The app performs calculation in several threads that send results to another thread as soon as a few prime numbers have been found.
Another thread has to be waiting for the prime numbers, pulls them (in order how prime number were found) and sends them for displaying through another thread (storing thread).
The storing thread should be implemented like a fake tcp-socket with states: establish connection, send prime numbers at a slow pace (to RecyclerView), closed connection, reconnection if the opened connection was unexpectedly closed.

Calculation and threading is backed with JavaRX

<img alt="UI" src="markdown_res/photo_2017-10-05_17-32-13.jpg" width="400" />


Direct link:
https://github.com/Quireg/PrimeNumbersCalculation/releases/download/1.0/app-release-unsigned.apk

## License

[MIT License][license]

[license]: https://github.com/quireg/PrimeNumbersCalculation/blob/master/MIT-LICENSE.txt


