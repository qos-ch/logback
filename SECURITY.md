
## Reporting security issues

Please report security issues related to the logback project to the
following email address:

   support(at)qos.ch


## Verifying contents

All logback project artifacts published on Maven central are signed. For each
artifact, there is an associated signature file with the .asc suffix.

The cryptographic key was updated 2022-08-08 to use a more modern
Elliptic curve algorithm instead of RSA previously.

### After 2022-08-08

To verify the signature use [this public key](https://www.slf4j.org/public-keys/60200AC4AE761F1614D6C46766D68DAA073BE985.gpg). Here is its fingerprint:
```
pub   nistp521 2022-08-08 [SC]
      60200AC4AE761F1614D6C46766D68DAA073BE985
uid   Ceki Gulcu <ceki@qos.ch>
sub   nistp521 2022-08-08 [E]
```

A copy of this key is stored on the
[keys.openpgp.org](https://keys.openpgp.org) keyserver. To add it to
your public key ring use the following command:

```
> FINGER_PRINT=60200AC4AE761F1614D6C46766D68DAA073BE985
> gpg  --keyserver hkps://keys.openpgp.org --recv-keys $FINGER_PRINT
```

### Before 2022-08-08

To verify the signature use [this public key](https://www.slf4j.org/public-keys/ceki-public-key.pgp). Here is its fingerprint:
```
pub   2048R/A511E325 2012-04-26
Key fingerprint = 475F 3B8E 59E6 E63A A780  6748 2C7B 12F2 A511 E325
uid   Ceki Gulcu <ceki@qos.ch>
sub   2048R/7FBFA159 2012-04-26
```

A copy of this key is stored on the
[keys.openpgp.org](https://keys.openpgp.org) keyserver. To add it to
your public key ring use the following command:

```
> FINGER_PRINT=475F3B8E59E6E63AA78067482C7B12F2A511E325
> gpg  --keyserver hkps://keys.openpgp.org --recv-keys $FINGER_PRINT
```


## Preventing commit history overwrite

In order to prevent loss of commit history, developers of the project
are highly encouraged to deny branch deletions or history overwrites
by invoking the following two commands on their local copy of the
repository.


```
git config receive.denyDelete true
git config receive.denyNonFastForwards true
```