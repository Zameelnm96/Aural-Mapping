import IPython.display as ipd
import librosa
import librosa.display
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import os
from keras.utils import to_categorical
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split

max_pad_length  = 174
#extracting mfcc features from audio files
def extract_mfcc_features(file_name):
    try:
        audio, sample_rate = librosa.load(file_name,res_type='kaiser_best')
        mfcc = librosa.feature.mfcc(y=audio, sr=sample_rate, n_mfcc=40)
        pad_length = max_pad_length - mfcc.shape[1]
        mfcc = np.pad(mfcc, pad_width=((0,0),(0, pad_length)),mode = 'constant')
        #mfcc_scaled = np.mean(mfcc.T, axis=0)
    except Exception as e:
        print('Error encountered during processing: '+ e)
        return None
    return mfcc

#print(extract_mfcc_features('D:\\Others\\Projects\\Deep\\Aural_Mapping_Project\\UrbanSound8K_Dataset\\audio\\fold1\\7061-6-0-0.wav').shape)
folder_path = 'D:\\Others\\Projects\\Deep\\Aural_Mapping_Project\\UrbanSound8K_Dataset\\audio\\'

#reading the metadata about the audio clips
meta_data = pd.read_csv('D:\\Others\\Projects\\Deep\\Aural_Mapping_Project\\UrbanSound8K_Dataset\\metadata\\UrbanSound8K.csv')

features = []
i = 0

#iterating through each audio files to extract mfcc features
for index, row in meta_data.iterrows():
    file_name = os.path.join(os.path.abspath(folder_path), 'fold'+str(row['fold'])+'\\'+str(row['slice_file_name']))
    data = extract_mfcc_features(file_name)
    class_label = row['class']
    features.append([data, class_label])
    i+=1
    #if i>=20:break - for debugging
    print(str(i))

#saving them as pandas dataframe
features_df = pd.DataFrame(features, columns=['feature','class_label'])

X = np.array(features_df.feature.tolist())
Y = np.array(features_df.class_label.tolist())
le = LabelEncoder()
Y = to_categorical(le.fit_transform(Y))

#splitting the data into train and test sets
train_x, test_x, train_y, test_y = train_test_split(X, Y, test_size = 0.2, random_state = 42 )

np.save('train_x_cnn.npy', train_x)
np.save('train_y_cnn.npy', train_y)
np.save('test_x_cnn.npy', test_x)
np.save('test_y_cnn.npy', test_y)
