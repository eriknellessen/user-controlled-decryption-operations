# Welcome

This software enables you to transparently encrypt your Dropbox folder on your PC. It also lets you confirm the decryption operations on an Android smartphone, which is used as an NFC-enabled token. Additionally, it supports sharing files with other Dropbox users, while still encrypting the shared data.

Warning: This is just proof-of-concept code and should _NOT_ be used in production environments

# Tested platforms:

* Debian Jessie (32 Bit) and Android Lollipop (5.0)

# Building

To build the software, execute the following commands:

```sh
git clone https://github.com/eriknellessen/user-controlled-decryption-operations
cd user-controlled-decryption-operations
make
```

# Using

## Android App

### Installing

To install the Android App on your smartphone, connect it to your PC, enable debugging and execute the following command:

```sh
cd Android
make install
```

### Setup

We use the Android smartphone just like an NFC-enabled smartcard. So just place your smartphone on your NFC reader.

You now need to generate a key on the smartphone/push an existing key to the smartphone. Please notice, that the key is not saved to the next usage of the App, see [issue #77 of jCardSim](https://github.com/licel/jcardsim/issues/77).

You can use `gpg` to generate/import the key. For a tutorial on importing keys to the smartphone, see [here](https://developers.yubico.com/PGP/Importing_keys.html). For a tutorial on generating a key on the smartphone, see [here](https://www.gnupg.org/howtos/card-howto/en/ch03s03.html).

## Transparent client-side encryption

### Setting up Dropbox

This needs to be done only once. It must be done before starting the transparent client-side encryption or Dropbox.

1. Create user Dropbox: `adduser Dropbox`
2. Install Dropbox (download *.deb from [here](https://www.dropbox.com/))
3. Start Dropbox as normal user, so the files are installed. When it asks for your e-mail, close dropbox.
4. Grant user Dropbox write access to your home directory, e.g. by executing `chmod 777 ~`
5. Execute `xhost +` (as normal user)
6. Start Dropbox (as user Dropbox)
7. Choose your home directory when asked where to place the Dropbox directory
8. Terminate Dropbox
9. Reclaim your Dropbox directory via chown
10. Remove all files in Dropbox, e.g. by executing `rm -rf ./* ./.*` inside the Dropbox directory

### Starting the transparent client-side encryption

This needs to be done before starting Dropbox.

To start the transparent client-side encryption, execute the following command:

```sh
bin/start_fuseecs.sh
```

### Starting Dropbox

This must not be done before starting the transparent client-side encryption.

To start Dropbox, first switch to the user Dropbox. Then start the program:

```sh
su Dropbox
/home/user/.dropbox-dist/dropbox-lnx.$PLATFORM-$VERSION/dropbox
```

## Sharing files

For sharing a folder, execute the following command:

```sh
bin/start_share_a_folder.sh $FOLDER $OPENPGP_FINGERPRINT
```

For example, the command could look like this:
```sh
bin/start_share_a_folder.sh /home/user/Dropbox/folder_to_share A6506F46
```

This shares the folder in a cryptographic way. Afterwards, you still have to share the folder via Dropbox.
