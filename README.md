Introduction
------------
* Works with Java 8 (1.8.0_181)
* Use Play 1.5.1

How to run
----------

1. Download play 1.5.1 (https://www.playframework.com/releases)
1. Put path to play in your PATH
1. `play deps --sync`
1. Go to module `app-base`, and delete the file `UADetector.java`

   ```rm modules/app-base-2.1.3/app/controllers/filters/UADetector.java``` 

1. `play run`
1. Visit `http://localhost:8080`
