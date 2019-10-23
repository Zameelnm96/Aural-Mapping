from keras.models import load_model
import numpy as np
import librosa


model = load_model('my_model3.h5')
max_pad_length  = 174

def extract_mfcc_features(file_name):
    try:
        audio, sample_rate = librosa.load(file_name,res_type='kaiser_best')
        #print(audio.shape)
        mfcc = librosa.feature.mfcc(y=audio, sr=sample_rate,hop_length=620, n_mfcc=40)
        #print(mfcc.shape)
        pad_length = max_pad_length - mfcc.shape[1]
        #if pad_length<0: pad_length=0
        #print(pad_length)
        mfcc = np.pad(mfcc, pad_width=((0,0),(0, pad_length)),mode = 'constant')
        #mfcc_scaled = np.mean(mfcc.T, axis=0)
    except Exception as e:
        print('Error encountered during processing: '+ e)
        return None
    return mfcc

classes= ['air_conditioner','car_horn','children_playing','dog_bark','drilling','engine_idling','gun_shot','jackhammer','siren','street_music']

def predict(filename):
    mfcc_feature = extract_mfcc_features(filename)
    mfcc_feature = mfcc_feature.reshape(1, 40, 174, 1)
    predict_vector = model.predict_classes(mfcc_feature)
    predict_class = classes[predict_vector[0]]
    print("==================================================="+'\n')
    print('The predicted class is: ', predict_class, '\n')

filename = 'D:\\Others\\Projects\\Deep\\Aural_Mapping_Project\\UrbanSound8K_Dataset\\audio\\fold4\\7389-1-2-3.wav'
fn = 'Siren-SoundBible.com-1094437108 (1).wav'
#extract_mfcc_features(fn)
predict(fn)
print("===================================================")
