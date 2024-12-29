package com.example.friends.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.friends.Fragments.callFragment;
import com.example.friends.Fragments.chatFragment;
import com.example.friends.Fragments.statusFragment;

import java.util.ArrayList;

public class fragment_adapter extends FragmentPagerAdapter {
    ArrayList<String> friends=new ArrayList<>();

    public fragment_adapter(@NonNull FragmentManager fm, ArrayList<String> friends) {
        super(fm);
        this.friends=friends;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:return new chatFragment(friends);
            case 1:return new statusFragment();
            case 2: return new callFragment();
            default:return new chatFragment(friends);
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title=null;
        switch (position){
            case 0:title="Chats";
                break;
            case 1:title="Status";
                break;
            case 2: title="Calls";
                break;
            default:title="Chats";
                break;
        }
        return title;
    }
}
