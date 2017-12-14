/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.example.fireeats.adapter;

import android.support.v7.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying the results of a Firestore {@link Query}.
 * <p>
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * {@link DocumentSnapshot#toObject(Class)} is not cached so the same object may be deserialized
 * many times as the user scrolls.
 * <p>
 * See the adapter classes in FirebaseUI (https://github.com/firebase/FirebaseUI-Android/tree/master/firestore) for a
 * more efficient implementation of a Firestore RecyclerView Adapter.
 */
public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements EventListener<QuerySnapshot> {

    private static final String TAG = "Firestore Adapter";

    private Query mQuery;
    private ListenerRegistration mRegistration;

    private ArrayList<DocumentSnapshot> mSnapshots = new ArrayList<>();

    public FirestoreAdapter(Query query) {
        mQuery = query;
    }

    public void startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(this);
        }
    }

    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

        mSnapshots.clear();
        notifyDataSetChanged();
    }

    public void setQuery(Query query) {
        // Stop listening
        stopListening();

        // Clear existinkodig data
        mSnapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        mQuery = query;
        startListening();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return mSnapshots.get(index);
    }

    protected void onError(FirebaseFirestoreException e) {
    }

    protected void onDataChanged() {
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e != null) return;

        for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
            DocumentSnapshot documentSnapshot = documentChange.getDocument();

            switch (documentChange.getType()) {
                case ADDED:
                    onDocumentAdded(documentChange);
                    break;

                case MODIFIED:
                    onDocumentModified(documentChange);
                    break;

                case REMOVED:
                    onDocumentRemoved(documentChange);
                    break;
            }
        }

        onDataChanged();
    }

    protected void onDocumentAdded(DocumentChange documentChange) {
        mSnapshots.add(documentChange.getNewIndex(), documentChange.getDocument());
        notifyItemInserted(documentChange.getNewIndex());
    }

    protected void onDocumentModified(DocumentChange documentChange) {
        if (documentChange.getOldIndex() == documentChange.getNewIndex()) {
            mSnapshots.set(documentChange.getOldIndex(), documentChange.getDocument());
            notifyItemChanged(documentChange.getOldIndex());
        } else {
            mSnapshots.remove(documentChange.getOldIndex());
            mSnapshots.add(documentChange.getNewIndex(), documentChange.getDocument());
            notifyItemMoved(documentChange.getOldIndex(), documentChange.getNewIndex());
        }
    }

    protected void onDocumentRemoved(DocumentChange documentChange) {
        mSnapshots.remove(documentChange.getOldIndex());
        notifyItemRemoved(documentChange.getOldIndex());
    }
}
