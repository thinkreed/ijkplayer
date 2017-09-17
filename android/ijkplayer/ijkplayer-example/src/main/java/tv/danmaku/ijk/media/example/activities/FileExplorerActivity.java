/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.example.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import tv.danmaku.ijk.media.example.R;
import tv.danmaku.ijk.media.example.application.AppActivity;
import tv.danmaku.ijk.media.example.application.Settings;
import tv.danmaku.ijk.media.example.eventbus.FileExplorerEvents;
import tv.danmaku.ijk.media.example.fragments.FileListFragment;

public class FileExplorerActivity extends AppActivity {
  private Settings mSettings;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    createSettingsIfNeed();

    String lastDirectory = mSettings.getLastDirectory();
    if (isDirectoryValid(lastDirectory)) {
      displayDirectory(lastDirectory, false);
    } else {
      displayDirectory("/", false);
    }
  }

  private boolean isDirectoryValid(String lastDirectory) {
    return !TextUtils.isEmpty(lastDirectory) && new File(lastDirectory).isDirectory();
  }

  private void createSettingsIfNeed() {
    if (mSettings == null) {
      mSettings = new Settings(this);
    }
  }

  @Override protected void onResume() {
    super.onResume();

    FileExplorerEvents.getBus().register(this);
  }

  @Override protected void onPause() {
    super.onPause();

    FileExplorerEvents.getBus().unregister(this);
  }

  private void displayDirectory(String path, boolean addToBackStack) {
    Fragment newFragment = FileListFragment.newInstance(path);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    transaction.replace(R.id.body, newFragment);

    if (addToBackStack) transaction.addToBackStack(null);
    transaction.commit();
  }

  @Subscribe public void onClickFile(FileExplorerEvents.OnClickFile event) {
    File f = event.mFile;
    f = ensureFileExists(f);

    if (f.isDirectory()) {
      mSettings.setLastDirectory(f.toString());
      displayDirectory(f.toString(), true);
      return;
    }

    if (isMediaFile(f)) {
      VideoActivity.intentTo(this, f.getPath(), f.getName());
    }
  }

  @NonNull private File ensureFileExists(File f) {
    try {
      f = f.getAbsoluteFile();
      f = f.getCanonicalFile();
      if (TextUtils.isEmpty(f.toString())) f = new File("/");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return f;
  }

  private boolean isMediaFile(File f) {
    return f.exists();
  }
}
