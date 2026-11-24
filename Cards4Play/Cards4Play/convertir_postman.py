import json
from pathlib import Path
import yaml

RAIZ = Path('postman/collections/Cards4Play API REST')
SALIDA = Path('postman/Cards4Play.postman_collection.json')

def leer(ruta):
    return yaml.safe_load(ruta.read_text(encoding='utf-8-sig'))

def convertir(ruta):
    d = leer(ruta)
    r = {'method': d['method'], 'header': [], 'url': d['url']}
    a = d.get('auth') or {}
    t = a.get('type')
    if t == 'basic':
        r['auth'] = {'type':'basic','basic':[{'key':k,'value':a['credentials'][k],'type':'string'} for k in ('username','password')]}
    elif t in (None,'noauth','none'):
        r['auth'] = {'type':'noauth'}
    else:
        raise ValueError(f'Autenticacion desconocida: {t} en {ruta}')
    b = d.get('body') or {}
    if b.get('type') == 'json':
        r['header'].append({'key':'Content-Type','value':'application/json','type':'text'})
        r['body'] = {'mode':'raw','raw':b['content'],'options':{'raw':{'language':'json'}}}
    elif b.get('type') not in (None,'none'):
        raise ValueError(f'Body desconocido en {ruta}')
    if d.get('headers'):
        raise ValueError(f'Revisar headers adicionales en {ruta}')
    eventos = [{'listen':{'beforeRequest':'prerequest','afterResponse':'test'}[s['type']], 'script':{'type':'text/javascript','exec':s['code'].splitlines()}} for s in d.get('scripts',[])]
    return {'name':ruta.name.removesuffix('.request.yaml'),'event':eventos,'request':r,'response':[]}

carpetas = sorted((p for p in RAIZ.iterdir() if p.is_dir() and p.name != '.resources'), key=lambda p:leer(p/'.resources/definition.yaml').get('order',0))
iniciales = {'baseUrl':'http://localhost:4567','adminUser':'admin','adminPassword':'admin123','clientUser':'','clientPassword':'cliente123'}
variables = [{'key':k,'value':iniciales.get(k,v or ''),'type':'string'} for k,v in leer(RAIZ/'.resources/definition.yaml').get('variables',{}).items()]
items = [{'name':p.name,'item':[convertir(f) for f in sorted(p.glob('*.request.yaml'),key=lambda f:leer(f).get('order',0))]} for p in carpetas]
coleccion = {'info':{'name':'Cards4Play API REST','description':'Coleccion de pruebas funcionales y de seguridad del proyecto Cards4Play. Credenciales de demostracion local.','schema':'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'},'variable':variables,'item':items}
n = sum(len(c['item']) for c in items)
t = sum('\n'.join(e['script']['exec']).count('pm.test(') for c in items for p in c['item'] for e in p['event'] if e['listen']=='test')
assert (len(items),n,t,len(variables)) == (7,31,163,13), f'Conteos inesperados: {len(items)}, {n}, {t}, {len(variables)}'
SALIDA.write_text(json.dumps(coleccion,indent=2,ensure_ascii=False),encoding='utf-8')
print('='*50)
print('CONVERSION COMPLETADA CORRECTAMENTE')
print('='*50)
print(f'Carpetas: {len(items)}\nPeticiones HTTP: {n}\nPruebas automatizadas: {t}\nVariables: {len(variables)}\nArchivo generado: {SALIDA}')
print('='*50)
