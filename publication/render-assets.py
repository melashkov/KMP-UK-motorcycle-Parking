"""Compose store graphics from actual app captures; never recreate app UI.
Run with the bundled Python runtime, or Python with Pillow installed.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parent
REPO=ROOT.parent
FONT=Path('/System/Library/Fonts/Supplemental')
def font(size,bold=False): return ImageFont.truetype(str(FONT/('Arial Bold.ttf' if bold else 'Arial.ttf')),size)
NAVY='#122B40'; BLUE='#247ED4'; CREAM='#F2F4F5'; MUTED='#BDD0DE'
shots=[
 ('01-map','Find a bay.\nPark your bike.','Explore motorcycle parking across the UK.'),
 ('02-details','Check the bay.\nPlan your stop.','See the parking type, then navigate or share.'),
 ('03-search-area','Explore somewhere\nnew.','Move the map, then search this area.'),
 ('04-add-bay','Found a bay?\nPut it on the map.','Send missing parking locations for review.'),
 ('05-suggest-edit','Help keep parking\nup to date.','Suggest corrections or report a removed bay.'),
 ('06-dark-map','A map that\nfits your day.','Choose light, dark or your device setting.'),
 ('07-dark-details','The details.\nIn dark mode.','Parking information and actions, in either theme.'),
 ('08-behind-app','Built by a rider.\nImproved by you.','Meet the story and community behind the app.'),
]
def wrap(draw,text,f,maxw):
 result=[]
 for para in text.split('\n'):
  line=''
  for word in para.split():
   candidate=(line+' '+word).strip()
   if draw.textlength(candidate,font=f)>maxw and line: result.append(line); line=word
   else: line=candidate
  result.append(line)
 return result

def compose(source,size,title,subtitle,number):
 w,h=size; s=w/1080
 canvas=Image.new('RGB',size,NAVY); d=ImageDraw.Draw(canvas)
 d.line([(int(w*.7),0),(w,int(h*.22))],fill='#25445B',width=int(28*s))
 pad=int(70*s)
 d.text((pad,int(63*s)),'UK MOTORCYCLE PARKING',font=font(int(27*s),True),fill=MUTED)
 y=int(132*s); tf=font(int(79*s),True)
 for line in wrap(d,title,tf,w-pad*2):
  d.text((pad,y),line,font=tf,fill='white'); y+=int(90*s)
 y+=int(26*s); sf=font(int(28*s))
 for line in wrap(d,subtitle,sf,w-pad*2):
  d.text((pad,y),line,font=sf,fill=MUTED); y+=int(37*s)
 top=max(y+int(44*s),int(h*.245))
 shot=Image.open(source).convert('RGB')
 aw=w-pad*2; ah=h-top-int(64*s)
 scale=min(aw/shot.width,ah/shot.height)
 shot=shot.resize((round(shot.width*scale),round(shot.height*scale)),Image.Resampling.LANCZOS)
 x=(w-shot.width)//2; sy=top+(ah-shot.height)//2
 frame=(x-int(10*s),sy-int(10*s),x+shot.width+int(10*s),sy+shot.height+int(10*s))
 d.rounded_rectangle(frame,radius=int(34*s),fill='#8294A2')
 canvas.paste(shot,(x,sy))
 d=ImageDraw.Draw(canvas)
 d.text((pad,h-int(36*s)),f'{number:02d}',font=font(int(18*s)),fill=MUTED)
 return canvas

def feature():
 img=Image.new('RGB',(1024,500),NAVY); d=ImageDraw.Draw(img)
 for pts in [[(710,-20),(840,240),(1020,410)],[(760,540),(660,320),(1020,160)]]:
  d.line(pts,fill='#294B63',width=20)
 logo=Image.open(REPO/'design-assets/app-icon-source.png').convert('RGB').resize((290,290),Image.Resampling.LANCZOS)
 img.paste(logo,(650,105))
 d.text((64,74),'UK MOTORCYCLE PARKING',font=font(24,True),fill=MUTED)
 d.text((64,138),'Find a bay.',font=font(65,True),fill='white')
 d.text((64,213),'Park your bike.',font=font(65,True),fill='white')
 d.text((64,332),'Built for riders across the UK.',font=font(25),fill=MUTED)
 img.save(ROOT/'google-play-feature-graphic.png')
 Image.open(REPO/'design-assets/app-icon-source.png').convert('RGB').resize((512,512),Image.Resampling.LANCZOS).save(ROOT/'google-play-icon.png')

def main():
 feature(); count=0
 for device,size in [('android',(1080,1920)),('iphone',(1320,2868)),('ipad',(2064,2752))]:
  for n,(slug,title,sub) in enumerate(shots,1):
   source=ROOT/'screenshots/raw'/device/(slug+'.png')
   if not source.exists(): continue
   folder=ROOT/'google-play-screenshots' if device=='android' else ROOT/'screenshots'/device
   out=folder/(slug+'.png'); out.parent.mkdir(parents=True,exist_ok=True)
   compose(source,size,title,sub,n).save(out)
   count+=1
 print(f'Created feature graphic, icon and {count} store screenshots.')
if __name__=='__main__': main()
