// IPhotoService.aidl
package com.example.link.photo;

// Declare any non-default types here with import statements

interface IPhotoService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void SetTimeInterval(int second);
    void SetRepeat(boolean isRepeat);
    void SetToken(String token, String secret);

}
