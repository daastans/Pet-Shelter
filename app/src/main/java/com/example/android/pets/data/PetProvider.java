package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class PetProvider extends ContentProvider {

    private static final int PET=101;
    private static final int PET_ID=102;
    private static final String LOG_TAG=PetProvider.class.getSimpleName();

    private static final UriMatcher uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    private PetDbHelper petDbHelper;

    static {
        uriMatcher.addURI(PetContract.PetEntry.CONTENT_AUTHORITY, PetContract.PetEntry.PATH_PETS,PET);
        uriMatcher.addURI(PetContract.PetEntry.CONTENT_AUTHORITY, PetContract.PetEntry.PATH_PETS +"/#",PET_ID);
    }
    @Override
    public boolean onCreate() {
        petDbHelper=new PetDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase sqLiteDatabase=petDbHelper.getReadableDatabase();

        Cursor cursor;
        int match=uriMatcher.match(uri);

        switch (match){
            case PET:
                cursor=sqLiteDatabase.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PET_ID:
                selection=PetContract.PetEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor=sqLiteDatabase.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);


            break;
            default:throw new IllegalArgumentException("argument illegal");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        int match=uriMatcher.match(uri);
        switch (match){
            case PET:
                return inserPet(uri,contentValues);
            default:
                throw new IllegalArgumentException();
        }

    }
    public static boolean isValidGender(int gender) {
        if (gender == PetContract.PetEntry.GENDER_UNKNOWN || gender == PetContract.PetEntry.GENDER_MALE || gender == PetContract.PetEntry.GENDER_FEMALE) {
            return true;
        }
        return false;
    }
    private Uri inserPet(Uri uri,ContentValues contentValues){
        String name=contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if(TextUtils.isEmpty(name)) throw new IllegalArgumentException("Pet requires valid name");

        Integer weight=contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if(weight<0) throw new IllegalArgumentException("Pet requires valid non negative weight");

        Integer gender=contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender==null || !isValidGender(gender)) throw new IllegalArgumentException("Pet requires valid gender");


        SQLiteDatabase sqLiteDatabase=petDbHelper.getWritableDatabase();
        Cursor cursor;
        long id=sqLiteDatabase.insert(PetContract.PetEntry.TABLE_NAME,null,contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int match=uriMatcher.match(uri);

        SQLiteDatabase sqLiteDatabase=petDbHelper.getWritableDatabase();

        switch (match){
            case PET:
                return  sqLiteDatabase.delete(PetContract.PetEntry.TABLE_NAME,s,strings);
            case PET_ID:
                s= PetContract.PetEntry._ID +"=?";
                strings=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return sqLiteDatabase.delete(PetContract.PetEntry.TABLE_NAME,s,strings);
                default:throw new IllegalArgumentException("invalid Match");

        }
    }
    private int updatePet(Uri uri,ContentValues contentValues,String selection , String[] selectionArgs){

        if (contentValues.size() == 0) {
            return 0;
        }





        if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)){
            String name=contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if(TextUtils.isEmpty(name)) throw new IllegalArgumentException("Pet should have a name");
        }
        else if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)){
            int weight=contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if(weight<0) throw new IllegalArgumentException("Weight should be positive or 0");
        }
        else if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)){
            int gender=contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if(!isValidGender(gender)) throw new IllegalArgumentException("Enter a valid gender");
        }

        SQLiteDatabase sqLiteDatabase=petDbHelper.getWritableDatabase();
        int numberOfcolumnsAffected=sqLiteDatabase.update(PetContract.PetEntry.TABLE_NAME,contentValues,selection,selectionArgs);

        return numberOfcolumnsAffected;


    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match=uriMatcher.match(uri);
        switch (match){
            case PET:
                return updatePet(uri,contentValues,selection,selectionArgs);
            case PET_ID:
                selection= PetContract.PetEntry._ID+"=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return  update(uri,contentValues,selection,selectionArgs);
                default:
                    throw new IllegalArgumentException("Invalid uri match");
        }

    }
}
