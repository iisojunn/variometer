# Variometer

[Project page](https://igorinov.com/android/variometer/)

[Simulation experiment](https://igorinov.com/android/variometer/howitworks.html)

Roger Labbe's [book](https://github.com/rlabbe/Kalman-and-Bayesian-Filters-in-Python) and [filterpy project](https://pypi.org/project/filterpy/) were used as a reference.

To be as efficient as possible (without using native C++ code), This implementation uses [EJML](https://ejml.org) in Procedural mode.
All arrays and matrices are allocated only once during filter initialization.

The app is available on [Google Play](https://play.google.com/store/apps/details?id=com.igorinov.variometer&hl=en_US).
